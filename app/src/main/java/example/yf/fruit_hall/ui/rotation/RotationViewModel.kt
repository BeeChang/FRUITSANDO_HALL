package example.yf.fruit_hall.ui.rotation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.R
import example.yf.fruit_hall.core.rotation.BreakSnapper
import example.yf.fruit_hall.core.rotation.BreakSpan
import example.yf.fruit_hall.core.rotation.Cell
import example.yf.fruit_hall.core.rotation.CellState
import example.yf.fruit_hall.core.rotation.FairnessPriority
import example.yf.fruit_hall.core.rotation.FrozenState
import example.yf.fruit_hall.core.rotation.HandoverMode
import example.yf.fruit_hall.core.rotation.PinnedAssignment
import example.yf.fruit_hall.core.rotation.RemainderPolicy
import example.yf.fruit_hall.core.rotation.RotationInput
import example.yf.fruit_hall.core.rotation.RotationOutput
import example.yf.fruit_hall.core.rotation.RotationScore
import example.yf.fruit_hall.core.rotation.SearchConfig
import example.yf.fruit_hall.core.rotation.SegmentType
import example.yf.fruit_hall.core.rotation.Slot
import example.yf.fruit_hall.core.rotation.TargetBasis
import example.yf.fruit_hall.core.rotation.Tier
import example.yf.fruit_hall.core.rotation.Violation
import example.yf.fruit_hall.core.rotation.Worker
import example.yf.fruit_hall.data.position.PositionRepository
import example.yf.fruit_hall.data.position.entity.MemberEntity
import example.yf.fruit_hall.data.rotation.RotationRepository
import example.yf.fruit_hall.data.schedule.ScheduleRepository
import example.yf.fruit_hall.data.rotation.entity.BreakAssignmentEntity
import example.yf.fruit_hall.data.rotation.entity.DayRoleAssignmentEntity
import example.yf.fruit_hall.data.rotation.entity.RotationPositionEntity
import example.yf.fruit_hall.data.rotation.entity.RotationSettingsEntity
import example.yf.fruit_hall.data.rotation.entity.ScheduleTemplateEntity
import example.yf.fruit_hall.data.rotation.entity.ShiftPresetEntity
import example.yf.fruit_hall.data.rotation.entity.ShiftRoleEntity
import example.yf.fruit_hall.data.rotation.toConstraintConfig
import example.yf.fruit_hall.data.rotation.toDomain
import example.yf.fruit_hall.data.rotation.toFairnessConfig
import example.yf.fruit_hall.data.rotation.toTierOrder
import example.yf.fruit_hall.data.rotation.toTimeConfig
import example.yf.fruit_hall.domain.rotation.PlanRotationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import javax.inject.Inject

// 스케줄 탭이 쓰는 키 형식. 그 탭과 반드시 같아야 오늘 데이터를 찾는다(monthKey "s202609", date "20260904").
private val SCHEDULE_MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM")
private val SCHEDULE_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
private val SHIFT_DISPLAY_ORDER = listOf("오픈", "오픈미들", "미들", "마감")

private data class RotationSnapshot(
    val members: List<MemberEntity>,
    val positions: List<RotationPositionEntity>,
    val presets: List<ShiftPresetEntity>,
    val roles: List<ShiftRoleEntity>,
    val settings: RotationSettingsEntity,
    val scheduleTemplates: List<ScheduleTemplateEntity> = emptyList()
)

@HiltViewModel
class RotationViewModel @Inject constructor(
    private val repository: RotationRepository,
    private val positionRepository: PositionRepository,
    private val scheduleRepository: ScheduleRepository,
    private val planRotation: PlanRotationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RotationUiState(date = today()))
    val uiState: StateFlow<RotationUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var dayPlanId: Long? = null
    /** dayPlanId가 어느 날짜 것인지. 매장 태블릿은 며칠씩 켜둔 채로 쓰므로 자정 넘김을 반드시 감지해야 한다. */
    private var dayPlanDate: String? = null
    private var currentPlanId: Long? = null
    private var lastOutput: RotationOutput? = null
    /**
     * 알고리즘이 실제로 돌아서 나온 결과인가. 사용자가 생성 전에 칸을 직접 채워도 결과 객체는 생기지만,
     * 그건 '생성됨'이 아니다 — 버튼 문구·커서·위반 배지는 이 값을 기준으로 한다.
     */
    private var planGenerated = false
    private var lastInput: RotationInput? = null
    private var pendingCursorOverride: Int? = null

    private var cachedMembers: List<MemberEntity> = emptyList()
    private var cachedPositions: List<RotationPositionEntity> = emptyList()
    private var cachedPresets: List<ShiftPresetEntity> = emptyList()
    private var cachedRoles: List<ShiftRoleEntity> = emptyList()
    private var cachedSettings: RotationSettingsEntity = RotationSettingsEntity()
    private var cachedRoleAssignments: List<DayRoleAssignmentEntity> = emptyList()
    private var cachedBreaks: List<BreakAssignmentEntity> = emptyList()
    private var cachedScheduleTemplates: List<ScheduleTemplateEntity> = emptyList()

    /**
     * 스케줄 탭(별도 DB)의 이번 달 근무표. 날이 바뀌면 표시할 날짜가 달라지고, 달이 바뀌면 구독 자체를
     * 다시 걸어야 해서 monthKey를 상태로 들고 flatMapLatest로 갈아탄다 — 태블릿을 며칠씩 켜두기 때문이다.
     */
    private val scheduleMonthKey = MutableStateFlow(monthKeyOf(LocalDate.now()))

    init {
        viewModelScope.launch {
            combine(
                positionRepository.observeMembers(),
                repository.observePositions(),
                repository.observePresets(),
                repository.observeRoles(),
                repository.observeSettings()
            ) { members, positions, presets, roles, settings ->
                RotationSnapshot(members, positions, presets, roles, settings)
            }.combine(repository.observeScheduleTemplates()) { snap, templates ->
                snap.copy(scheduleTemplates = templates)
            }.collect { snap ->
                cachedMembers = snap.members.filter { !it.isDeleted }
                cachedPositions = snap.positions
                cachedPresets = snap.presets
                cachedRoles = snap.roles
                cachedSettings = snap.settings
                cachedScheduleTemplates = snap.scheduleTemplates

                if (!initialized) {
                    initialized = true
                    val presetId = snap.presets.firstOrNull { it.isDefault }?.id ?: snap.presets.firstOrNull()?.id
                    if (presetId != null) ensureDayPlan(presetId)
                }
                syncFromCache()
            }
        }

        viewModelScope.launch {
            scheduleMonthKey
                .flatMapLatest { scheduleRepository.observeEntriesForMonth(it) }
                .collect { entries ->
                    // 스케줄 탭은 쉬는 날도 "off"로 저장한다 — 오늘 실제로 나오는 사람만 남긴다.
                    val todayKey = LocalDate.now().format(SCHEDULE_DATE_FORMAT)
                    val todays = entries
                        .filter { it.date == todayKey && it.shift != "off" }
                        .map { TodayShiftUi(it.personName, it.shift) }
                        .sortedWith(compareBy({ SHIFT_DISPLAY_ORDER.indexOf(it.shift).takeIf { i -> i >= 0 } ?: Int.MAX_VALUE }, { it.personName }))
                    _uiState.update { it.copy(todayShifts = todays) }
                }
        }
    }

    private suspend fun ensureDayPlan(presetId: Long) {
        val date = today()
        // 같은 날 프리셋을 바꾸면 이전 프리셋의 오늘 배정은 더 이상 유효하지 않다. 그대로 병합하면
        // 줄 이름이 겹칠 때(자동 이름 '근무1'은 프리셋마다 똑같다) 이전 프리셋의 시각·담당자가 그대로
        // 끌려와서 "프리셋을 바꿔도 아무것도 안 바뀌는" 상태가 된다.
        val presetChanged = repository.getDayPlan(date)?.presetId?.let { it != presetId } ?: false
        val previousDayPlanId = dayPlanId
        val dayPlan = repository.getOrCreateDayPlan(date, presetId, System.nanoTime(), "")
        dayPlanId = dayPlan.id
        dayPlanDate = date
        scheduleMonthKey.value = monthKeyOf(LocalDate.now())

        // lastOutput은 특정 dayPlan에 속한 결과다. 날짜가 넘어가 dayPlan이 바뀌었는데 그대로 들고 있으면
        // "예전 결과표 + 새로 비워진 배정"이 겹쳐 표의 모든 칸이 빈칸으로 보인다(표가 통째로 사라진 것처럼).
        // 프리셋을 바꾼 경우에는 이전 편성으로 짠 결과 자체가 무의미하므로 DB에서도 지운다.
        if (presetChanged) repository.clearPlans(dayPlan.id)
        if (presetChanged || previousDayPlanId != dayPlan.id) clearPlanState()

        val rolesForPreset = cachedRoles.filter { it.presetId == presetId }
        val existingByLabel =
            if (presetChanged) emptyMap() else repository.getRoleAssignments(dayPlan.id).associateBy { it.roleLabel }
        val merged = rolesForPreset.map { role ->
            val prev = existingByLabel[role.label]
            DayRoleAssignmentEntity(
                id = prev?.id ?: 0,
                dayPlanId = dayPlan.id,
                roleLabel = role.label,
                memberId = prev?.memberId,
                startMin = prev?.startMin ?: role.startMin,
                endMin = prev?.endMin ?: role.endMin,
                scheduleLabel = prev?.scheduleLabel,
                excludedFromAssign = prev?.excludedFromAssign ?: false
            )
        }
        repository.saveRoleAssignments(dayPlan.id, merged)
        cachedRoleAssignments = merged
        cachedBreaks = repository.getBreaks(dayPlan.id)

        _uiState.update { it.copy(selectedPresetId = presetId, date = date) }

        val restored = repository.loadLatestPlan(dayPlan.id)
        if (restored != null) {
            val input = buildInput(fullRegen = true, seedOverride = restored.seed)
            lastInput = input
            val scored = RotationScore.evaluate(input, restored.slots, restored.cells)
            lastOutput = RotationOutput(
                restored.slots, restored.cells, scored.score, scored.violations,
                scored.debtCurve, scored.targetCurve, restored.seed
            )
            currentPlanId = restored.planId
            planGenerated = true
            applyOutput(lastOutput!!)
        }
    }

    private fun syncFromCache() {
        _uiState.update { current ->
            val next = current.copy(
                members = cachedMembers.map { MemberUi(it.id, it.name, it.colorHex) },
                positions = cachedPositions.map { it.toUi() },
                presets = cachedPresets.map { RotationPresetUi(it.id, it.name, it.isDefault, it.alphaOverride) },
                scheduleTemplates = cachedScheduleTemplates.map { it.toUi() },
                settings = cachedSettings.toUi(),
                roleAssignments = buildRoleAssignmentUi(),
                breaks = buildBreakUi()
            )
            // 아직 배정 결과가 없으면 근무시간만 칠한 초안 표를 같은 자리에 그린다.
            val output = lastOutput
            if (output == null) {
                val (draftSlots, draftRows) = buildDraftTable()
                next.copy(slots = draftSlots, rows = draftRows, isDraft = true)
            } else {
                // 생성 후에도 근무스케줄·브레이크를 라이브로 반영한다 — 포지션 배치는 재생성 전까지
                // 그대로 두되(§10-6), 시간 표시·근무/휴게 칸은 재생성 없이 바로 갱신되어야 드롭이
                // 실제로 반영됐는지 사용자가 알 수 있다.
                val slotsUi = output.slots.sortedBy { it.index }
                    .map { RotationSlotUi(it.index, it.startMin, it.endMin, it.type, it.isFrozen, it.remainderNote) }
                next.copy(slots = slotsUi, rows = buildRows(slotsUi, output, next.violations), isDraft = !planGenerated)
            }
        }
    }

    /**
     * 생성 전 초안 표. **가로 칸은 실제 배정에 쓰일 슬롯 그대로다**(§4 앵커 → 구간 → 분할).
     * 예전엔 30분 고정 격자를 따로 그렸는데, 그러면 '포지션 배정하기'를 누르는 순간 칸 자체가
     * 통째로 바뀌어 표가 다시 짜인 것처럼 보인다. 생성 전후로 달라지는 건 칸의 내용뿐이어야 한다.
     */
    private fun buildDraftTable(): Pair<List<RotationSlotUi>, List<RotationRowUi>> {
        val slots = draftGrid().map {
            RotationSlotUi(it.index, it.startMin, it.endMin, it.type, isFrozen = false, remainderNote = it.remainderNote)
        }
        return slots to buildRows(slots, output = null, violations = emptyList())
    }

    /**
     * 지금 입력(근무시간·브레이크·설정)으로 실제 배정에 쓰일 슬롯을 계산한다. 알고리즘을 돌리는 게 아니라
     * 시간축만 만드는 순수 계산이라 가볍다. 설정이 뒤집혀 그리드가 비면 표가 통째로 사라지므로
     * 최소 한 칸은 만들어 돌려준다.
     */
    private fun draftGrid(): List<Slot> {
        val grid = runCatching { BreakSnapper.snap(buildInput(fullRegen = true, seedOverride = 0L)).grid }
            .getOrDefault(emptyList())
        if (grid.isNotEmpty()) return grid
        val start = cachedSettings.windowStart
        val end = if (cachedSettings.windowEnd > start) cachedSettings.windowEnd else start + 60
        return listOf(Slot(0, start, end, SegmentType.PRE_BREAK))
    }

    /**
     * 아직 생성하지 않았는데 사용자가 칸을 직접 고칠 때 쓸 뼈대. 근무/휴게/비번만 채워진 빈 결과다 —
     * 알고리즘을 먼저 돌리라고 막아세우지 않기 위한 것이다.
     */
    private fun buildSkeletonOutput(): RotationOutput {
        val slots = draftGrid()
        val breakByMember = cachedBreaks.associateBy { it.memberId }
        val cells = todaysWorkers().flatMap { assignment ->
            val memberId = assignment.memberId!!
            val span = breakByMember[memberId]
            slots.map { slot ->
                val working = slot.startMin < assignment.endMin && slot.endMin > assignment.startMin
                val onBreak = span != null && slot.startMin < span.endMin && slot.endMin > span.startMin
                Cell(
                    slotIndex = slot.index,
                    memberId = memberId,
                    positionId = null,
                    state = when {
                        !working -> CellState.OFF
                        onBreak -> CellState.BREAK
                        else -> CellState.ASSIGNED
                    }
                )
            }
        }
        return RotationOutput(slots, cells, 0L, emptyList(), emptyMap(), emptyList(), 0L)
    }

    private fun buildRoleAssignmentUi(): List<RoleAssignmentUi> {
        val memberById = cachedMembers.associateBy { it.id }
        return cachedRoleAssignments.map { ra ->
            RoleAssignmentUi(
                roleLabel = ra.roleLabel,
                memberId = ra.memberId,
                memberName = ra.memberId?.let { memberById[it] }?.name,
                startMin = ra.startMin,
                endMin = ra.endMin
            )
        }
    }

    private fun buildBreakUi(): List<BreakUi> {
        val memberById = cachedMembers.associateBy { it.id }
        return cachedBreaks.map { b ->
            BreakUi(
                memberId = b.memberId,
                memberName = memberById[b.memberId]?.name ?: "?",
                startMin = b.startMin,
                endMin = b.endMin,
                isManual = b.isManual
            )
        }
    }

    // ── 프리셋 선택 ──
    fun selectPreset(id: Long) {
        if (_uiState.value.selectedPresetId == id) return
        viewModelScope.launch {
            ensureDayPlan(id)
            syncFromCache()
        }
    }

    // ── 포지션 관리 (§3-1) ──
    fun savePosition(ui: RotationPositionUi) = viewModelScope.launch { repository.upsertPosition(ui.toEntity()) }
    fun deletePosition(id: Long) = viewModelScope.launch {
        cachedPositions.firstOrNull { it.id == id }?.let { repository.deletePosition(it) }
    }
    fun setPositionActive(id: Long, isActive: Boolean) = viewModelScope.launch { repository.setPositionActive(id, isActive) }

    // ── 프리셋/역할 관리 (§3-2) ──
    fun savePreset(id: Long, name: String, alphaOverride: Double?) = viewModelScope.launch {
        val newId = repository.upsertPreset(ShiftPresetEntity(id = id, name = name, alphaOverride = alphaOverride))
        if (_uiState.value.selectedPresetId == null) selectPreset(newId)
    }
    fun deletePreset(id: Long) = viewModelScope.launch {
        cachedPresets.firstOrNull { it.id == id }?.let { repository.deletePreset(it) }
    }
    // savePreset(id, ...)을 재사용하지 않는다 — 그건 alphaOverride까지 통째로 덮어써서 isDefault도 false로
    // 초기화된다. 이름만 바꿀 때는 기존 값을 그대로 복사해 name만 교체한다.
    fun renamePreset(id: Long, name: String) = viewModelScope.launch {
        val existing = cachedPresets.firstOrNull { it.id == id } ?: return@launch
        if (name.isBlank() || name == existing.name) return@launch
        repository.upsertPreset(existing.copy(name = name))
    }
    fun setDefaultPreset(id: Long) = viewModelScope.launch { repository.setDefaultPreset(id) }

    // ── 표의 근무 줄 (§3-2 ShiftRole) ──
    // 별도 관리 화면 없이 표에서 바로 추가·삭제한다. 이름은 배정을 잇는 내부 키일 뿐이라 자동으로 붙인다 —
    // 사용자가 채워야 할 건 '누가'(이름 칸 드롭)와 '몇 시부터'(근무스케줄 칩 드롭)뿐이다.
    fun addScheduleRow() = viewModelScope.launch {
        val presetId = _uiState.value.selectedPresetId ?: createDefaultPreset()
        val existing = cachedRoles.filter { it.presetId == presetId }
        val label = generateSequence(existing.size + 1) { it + 1 }
            .map { "근무$it" }
            .first { candidate -> existing.none { it.label == candidate } }
        val start = cachedSettings.windowStart
        val end = (start + cachedSettings.defaultShiftDurationMinutes).coerceAtMost(29 * 60 + 59)
        val entity = ShiftRoleEntity(
            presetId = presetId, label = label, startMin = start, endMin = end, sortOrder = existing.size
        )
        val newId = repository.upsertRole(entity)
        // ensureDayPlan이 cachedRoles를 읽으므로 DB Flow가 돌아오기 전에 직접 갱신해 둔다 —
        // 안 그러면 방금 추가한 줄이 빠진 채로 병합돼 표에 안 나타난다.
        cachedRoles = cachedRoles + entity.copy(id = newId)
        ensureDayPlan(presetId)
        syncFromCache()
    }

    fun deleteScheduleRow(roleLabel: String) = viewModelScope.launch {
        val presetId = _uiState.value.selectedPresetId ?: return@launch
        val role = cachedRoles.firstOrNull { it.presetId == presetId && it.label == roleLabel } ?: return@launch
        // 지우는 줄에 꽂혀 있던 사람이 오늘 다른 줄에 없다면 그 사람 브레이크도 같이 치운다 —
        // 남겨두면 나중에 그 사람을 다른 줄에 넣었을 때 예전 휴게가 유령처럼 따라붙는다.
        val removedMemberId = cachedRoleAssignments.firstOrNull { it.roleLabel == roleLabel }?.memberId
        val stillAssigned = cachedRoleAssignments.any { it.roleLabel != roleLabel && it.memberId == removedMemberId }
        repository.deleteRole(role)
        cachedRoles = cachedRoles.filterNot { it.id == role.id }
        if (removedMemberId != null && !stillAssigned) {
            val dpId = dayPlanId
            cachedBreaks = cachedBreaks.filterNot { it.memberId == removedMemberId }
            if (dpId != null) repository.saveBreaks(dpId, cachedBreaks)
        }
        ensureDayPlan(presetId)
        syncFromCache()
    }

    /** 프리셋이 하나도 없을 때 자동 생성 — 프리셋을 먼저 만들라고 막아세우지 않는다. */
    private suspend fun createDefaultPreset(): Long {
        val entity = ShiftPresetEntity(name = "기본", isDefault = true)
        val newId = repository.upsertPreset(entity)
        cachedPresets = cachedPresets + entity.copy(id = newId)
        _uiState.update { it.copy(selectedPresetId = newId) }
        return newId
    }

    // ── 근무 스케줄 템플릿 (프리셋 무관, 재사용) ──
    fun saveScheduleTemplate(template: RotationScheduleTemplateUi) = viewModelScope.launch {
        repository.upsertScheduleTemplate(
            ScheduleTemplateEntity(
                id = template.id, label = template.label, startMin = template.startMin, endMin = template.endMin,
                breakStartMin = template.breakStartMin, breakMinutes = template.breakMinutes, sortOrder = template.sortOrder
            )
        )
    }
    fun deleteScheduleTemplate(template: RotationScheduleTemplateUi) = viewModelScope.launch {
        repository.deleteScheduleTemplate(
            ScheduleTemplateEntity(template.id, template.label, template.startMin, template.endMin, template.breakStartMin, template.breakMinutes, template.sortOrder)
        )
    }

    // ── 멤버 (position 탭과 데이터 공유 — 여기서 직접 추가/삭제 가능) ──
    fun addMember(name: String, colorHex: String) = viewModelScope.launch {
        positionRepository.addMember(MemberEntity(name = name, colorHex = colorHex))
    }
    fun deleteMember(id: Long) = viewModelScope.launch { positionRepository.deleteMember(id) }

    // ── 오늘 인원배정 (§3-3) ──
    /**
     * dayPlanId가 아직 없으면(프리셋을 막 만든 직후 등) 지금 만든다. 이게 없으면 표에는 근무조가 보이는데
     * 드래그 배정·시간 수정이 전부 조용히 무시돼 "인원을 넣을 방법이 없는" 막다른 상태가 된다.
     */
    private suspend fun requireDayPlanId(): Long? {
        val presetId = _uiState.value.selectedPresetId ?: return null
        // 날짜가 넘어갔는데 어제 dayPlanId로 계속 쓰면, 저장은 어제 것에 되고 화면은 오늘 것을 읽어
        // "방금 넣은 게 통째로 사라지는" 상태가 된다. 상시 거치 태블릿이라 실제로 매일 밤 발생한다.
        if (dayPlanId == null || dayPlanDate != today()) ensureDayPlan(presetId)
        return dayPlanId
    }

    /** 활성 근무조 중 오늘 배정 행이 빠진 것을 채운다 — 표에 보이는 줄은 전부 배정 대상이어야 한다. */
    private fun ensureAssignmentRows(dayPlanId: Long) {
        val presetId = _uiState.value.selectedPresetId
        val existing = cachedRoleAssignments.map { it.roleLabel }.toSet()
        val missing = cachedRoles
            .filter { it.presetId == presetId && it.label !in existing }
            .map {
                DayRoleAssignmentEntity(
                    dayPlanId = dayPlanId, roleLabel = it.label, memberId = null,
                    startMin = it.startMin, endMin = it.endMin
                )
            }
        if (missing.isNotEmpty()) cachedRoleAssignments = cachedRoleAssignments + missing
    }

    /**
     * 배정 초기화 (§[RotationResetTarget]). 어떤 범위를 골라도 생성 결과(포지션)는 항상 지운다 —
     * 사람이나 근무시간이 바뀌면 예전 결과표는 이미 그 편성의 표가 아니라서, 남겨두면
     * "미배정인데 예전 포지션이 그대로 있는" 모순된 화면이 된다.
     */
    fun resetAssignments(target: RotationResetTarget) = viewModelScope.launch {
        val dpId = requireDayPlanId() ?: return@launch
        val clearMembers = target == RotationResetTarget.MEMBERS || target == RotationResetTarget.ALL
        val clearSchedules = target == RotationResetTarget.SCHEDULES || target == RotationResetTarget.ALL

        if (clearMembers || clearSchedules) {
            val roleByLabel = cachedRoles
                .filter { it.presetId == _uiState.value.selectedPresetId }
                .associateBy { it.label }
            cachedRoleAssignments = cachedRoleAssignments.map { assignment ->
                val role = roleByLabel[assignment.roleLabel]
                assignment.copy(
                    memberId = if (clearMembers) null else assignment.memberId,
                    // 근무스케줄 초기화는 근무조에 저장된 원래 시각으로 되돌리는 것이다.
                    startMin = if (clearSchedules) role?.startMin ?: assignment.startMin else assignment.startMin,
                    endMin = if (clearSchedules) role?.endMin ?: assignment.endMin else assignment.endMin,
                    scheduleLabel = if (clearSchedules) null else assignment.scheduleLabel
                )
            }
            repository.saveRoleAssignments(dpId, cachedRoleAssignments)

            // 브레이크는 "누가 언제 쉬는지"라 사람에도 시간에도 딸려 있다 — 둘 중 하나를 지우면 같이 지운다.
            cachedBreaks = emptyList()
            repository.saveBreaks(dpId, cachedBreaks)
        }

        clearPlanState()
        _uiState.update {
            it.copy(
                score = null, violations = emptyList(),
                debtCurve = emptyMap(), targetCurve = emptyList(), selectedCell = null
            )
        }
        syncFromCache()
        repository.clearPlans(dpId)
    }

    /**
     * 표에는 그대로 두고 포지션 배정에서만 빼기(교육·행사 지원 등). 근무시간·휴게는 건드리지 않는다.
     * 이미 만들어둔 결과에서 그 사람 칸을 비워야 화면과 실제 배정이 어긋나지 않는다.
     */
    fun toggleExcludeFromAssign(roleLabel: String) = viewModelScope.launch {
        val dpId = requireDayPlanId() ?: return@launch
        ensureAssignmentRows(dpId)
        val target = cachedRoleAssignments.firstOrNull { it.roleLabel == roleLabel } ?: return@launch
        val excluded = !target.excludedFromAssign
        cachedRoleAssignments = cachedRoleAssignments.map {
            if (it.roleLabel == roleLabel) it.copy(excludedFromAssign = excluded) else it
        }
        val memberId = target.memberId
        if (excluded && memberId != null) {
            lastOutput?.let { output ->
                val cells = output.cells.map { cell ->
                    if (cell.memberId == memberId && cell.state == CellState.ASSIGNED)
                        cell.copy(positionId = null, isPinned = false) else cell
                }
                lastOutput = rescored(output.copy(cells = cells))
                lastOutput?.let { applyOutput(it) }
            }
        }
        syncFromCache()
        repository.saveRoleAssignments(dpId, cachedRoleAssignments)
        val out = lastOutput
        if (excluded && memberId != null && currentPlanId != null && out != null) persistPlan(dpId, out)
    }

    fun updateRoleTime(roleLabel: String, startMin: Int, endMin: Int) = viewModelScope.launch {
        updateRoleTimeInternal(roleLabel, startMin, endMin, scheduleLabel = null)
    }

    private suspend fun updateRoleTimeInternal(roleLabel: String, startMin: Int, endMin: Int, scheduleLabel: String?) {
        val dpId = requireDayPlanId() ?: return
        ensureAssignmentRows(dpId)
        val updated = cachedRoleAssignments.map {
            if (it.roleLabel == roleLabel) it.copy(startMin = startMin, endMin = endMin, scheduleLabel = scheduleLabel) else it
        }
        cachedRoleAssignments = updated
        syncFromCache()
        repository.saveRoleAssignments(dpId, updated)
    }

    // ── 브레이크 (§3-4) ──
    // 근무 스케줄에는 더 이상 고정 브레이크 시각을 두지 않는다(사람마다·날마다 다를 수 있어서).
    // 대신 오늘 배정된 사람 중 아직 브레이크가 없는 사람에게만 설정의 기본 시각으로 채워 넣는다 —
    // 이미 손으로 맞춰둔 브레이크(예외)는 건드리지 않는다.
    fun generateDefaultBreaks() = viewModelScope.launch {
        val dpId = requireDayPlanId() ?: return@launch
        val presetId = _uiState.value.selectedPresetId
        val activeLabels = cachedRoles.filter { it.presetId == presetId }.map { it.label }.toSet()
        val assignedMemberIds = cachedRoleAssignments
            .filter { it.roleLabel in activeLabels && it.memberId != null }
            .mapNotNull { it.memberId }.distinct()
        val existingIds = cachedBreaks.map { it.memberId }.toSet()
        val start = cachedSettings.defaultBreakStartMin
        val additions = assignedMemberIds.filterNot { it in existingIds }
            .map { BreakAssignmentEntity(dayPlanId = dpId, memberId = it, startMin = start, endMin = start + 60) }
        val updated = cachedBreaks + additions
        cachedBreaks = updated
        syncFromCache()
        repository.saveBreaks(dpId, updated)
    }

    fun updateBreak(memberId: Long, startMin: Int, endMin: Int) = viewModelScope.launch {
        updateBreakInternal(memberId, startMin, endMin)
    }

    private suspend fun updateBreakInternal(memberId: Long, startMin: Int, endMin: Int) {
        val dpId = requireDayPlanId() ?: return
        val exists = cachedBreaks.any { it.memberId == memberId }
        val updated = if (exists) {
            cachedBreaks.map { if (it.memberId == memberId) it.copy(startMin = startMin, endMin = endMin, isManual = true) else it }
        } else {
            cachedBreaks + BreakAssignmentEntity(dayPlanId = dpId, memberId = memberId, startMin = startMin, endMin = endMin, isManual = true)
        }
        cachedBreaks = updated
        syncFromCache()
        repository.saveBreaks(dpId, updated)
    }

    /**
     * 표에서 어떤 칸을 휴게로 만들 때. 이미 그 칸에 걸쳐 있는 브레이크가 있으면 넓히고,
     * 다른 시간대에 있으면 **통째로 그 칸으로 옮긴다** — 한 사람이 브레이크를 두 번 갖지 않게 한다.
     */
    private suspend fun moveBreakToSlot(memberId: Long, startMin: Int, endMin: Int) {
        val dpId = requireDayPlanId() ?: return
        val existing = cachedBreaks.firstOrNull { it.memberId == memberId }
        // 끝점만 맞닿은 건 겹친 게 아니다. 이걸 겹침으로 보면 "4시 브레이크인데 3시를 휴게로" 했을 때
        // 3시~5시 두 시간으로 넓어져서, 옮기려던 사람이 두 시간을 쉬게 된다.
        val overlaps = existing != null && existing.startMin < endMin && existing.endMin > startMin
        val span = BreakAssignmentEntity(
            dayPlanId = dpId,
            memberId = memberId,
            startMin = if (overlaps) minOf(existing!!.startMin, startMin) else startMin,
            endMin = if (overlaps) maxOf(existing!!.endMin, endMin) else endMin,
            isManual = true
        )
        cachedBreaks = cachedBreaks.filterNot { it.memberId == memberId } + span
        syncFromCache()
        repository.saveBreaks(dpId, cachedBreaks)
    }

    /** 휴게 칸을 포지션으로 바꿀 때. 그 칸만큼 브레이크 시각에서 덜어낸다(다 덜어내면 브레이크 없음). */
    private suspend fun clearBreakOverSlot(memberId: Long, startMin: Int, endMin: Int) {
        val dpId = requireDayPlanId() ?: return
        val existing = cachedBreaks.firstOrNull { it.memberId == memberId } ?: return
        if (existing.startMin >= endMin || existing.endMin <= startMin) return
        val remaining = when {
            // 칸이 브레이크 한가운데를 뚫는 경우 — 한 칸으로는 표현이 안 되니 더 긴 쪽만 남긴다.
            existing.startMin < startMin && existing.endMin > endMin ->
                if (startMin - existing.startMin >= existing.endMin - endMin) existing.copy(endMin = startMin, isManual = true)
                else existing.copy(startMin = endMin, isManual = true)
            existing.startMin < startMin -> existing.copy(endMin = startMin, isManual = true)
            existing.endMin > endMin -> existing.copy(startMin = endMin, isManual = true)
            else -> null // 브레이크가 이 칸 안에 다 들어감 → 오늘 브레이크 없음
        }
        cachedBreaks = cachedBreaks.filterNot { it.memberId == memberId } + listOfNotNull(remaining)
        syncFromCache()
        repository.saveBreaks(dpId, cachedBreaks)
    }

    fun removeBreak(memberId: Long) = viewModelScope.launch {
        val dpId = requireDayPlanId() ?: return@launch
        val updated = cachedBreaks.filterNot { it.memberId == memberId }
        cachedBreaks = updated
        syncFromCache()
        repository.saveBreaks(dpId, updated)
    }

    // ── 설정 (§10-7) ──
    fun updateSettings(ui: RotationSettingsUi) {
        cachedSettings = ui.toEntity()
        syncFromCache()
        viewModelScope.launch { repository.updateSettings(cachedSettings) }
    }

    // ── 다이얼로그 토글 ──
    fun setDialog(
        showMemberManage: Boolean? = null,
        showPresetManage: Boolean? = null,
        showBreakEdit: Boolean? = null,
        showSettings: Boolean? = null,
        showCursorPicker: Boolean? = null
    ) {
        _uiState.update {
            it.copy(
                showMemberManage = showMemberManage ?: it.showMemberManage,
                showPresetManage = showPresetManage ?: it.showPresetManage,
                showBreakEdit = showBreakEdit ?: it.showBreakEdit,
                showSettings = showSettings ?: it.showSettings,
                showCursorPicker = showCursorPicker ?: it.showCursorPicker
            )
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    // ── 생성 (§7) ──
    fun regenerateFull() = generate(fullRegen = true)

    // cursorMinOverride가 없으면 현재 시각 기준으로, 있으면 사용자가 고른 시각 기준으로
    // 그 이후 구간만 다시 배정한다 (핀 고정된 칸은 core/LocalSearch가 건드리지 않는다).
    fun regenerateFromCursor(cursorMinOverride: Int? = null) {
        pendingCursorOverride = cursorMinOverride
        generate(fullRegen = false)
    }

    private fun generate(fullRegen: Boolean) {
        val activeLabels = cachedRoles
            .filter { it.presetId == _uiState.value.selectedPresetId }
            .map { it.label }.toSet()
        if (activeLabels.isEmpty()) {
            _uiState.update { it.copy(errorMessage = RotationMessage.Res(R.string.rotation_error_no_schedule)) }
            return
        }
        if (todaysWorkers().isEmpty()) {
            _uiState.update { it.copy(errorMessage = RotationMessage.Res(R.string.rotation_error_no_member)) }
            return
        }
        viewModelScope.launch {
            // dayPlanId가 아직 없어도 조용히 멈추지 않는다 — 여기서 만들어서라도 생성까지 간다.
            val dpId = requireDayPlanId() ?: run {
                _uiState.update { it.copy(errorMessage = RotationMessage.Res(R.string.rotation_error_no_preset)) }
                return@launch
            }
            _uiState.update { it.copy(isGenerating = true, errorMessage = null) }
            try {
                val seed = if (fullRegen) System.nanoTime() else (lastOutput?.seed ?: System.nanoTime())
                val input = buildInput(fullRegen, seed)
                lastInput = input
                val output = planRotation(input)
                lastOutput = output
                planGenerated = true
                val cursor = input.frozen?.freezeCursorMin ?: input.timeConfig.windowStart
                currentPlanId = repository.savePlan(dpId, cursor, output)
                applyOutput(output)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                        errorMessage = e.message?.let(RotationMessage::Raw)
                            ?: RotationMessage.Res(R.string.rotation_error_generate_failed)
                    ) }
            } finally {
                pendingCursorOverride = null
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    /**
     * 오늘 실제로 계산에 들어갈 배정만 추린다. 멤버가 삭제됐는데 배정에 id만 남아 있으면
     * 알고리즘은 그 유령을 한 자리 차지시키고 표는 그 줄을 못 그려서 자리가 새는 것처럼 보인다.
     */
    private fun todaysWorkers(): List<DayRoleAssignmentEntity> {
        val labels = cachedRoles.filter { it.presetId == _uiState.value.selectedPresetId }.map { it.label }.toSet()
        val memberIds = cachedMembers.map { it.id }.toSet()
        return cachedRoleAssignments.filter {
            it.roleLabel in labels && it.memberId in memberIds && !it.excludedFromAssign
        }
    }

    private fun buildInput(fullRegen: Boolean, seedOverride: Long): RotationInput {
        val assignments = todaysWorkers()
        val workers = assignments.map { Worker(it.memberId!!, it.startMin, it.endMin) }
        // 오늘 근무하지 않는 사람의 브레이크는 앵커로 쓰면 안 된다 — 아무도 안 비우는 시각에
        // 슬롯 경계가 생겨 그리드가 쓸데없이 쪼개진다(§4-1).
        val workingIds = workers.map { it.memberId }.toSet()
        val breakSpans = cachedBreaks.filter { it.memberId in workingIds }
            .map { BreakSpan(it.memberId, it.startMin, it.endMin) }
        val positions = cachedPositions.map { it.toDomain() }
        val preset = cachedPresets.firstOrNull { it.id == _uiState.value.selectedPresetId }
        val timeConfig = cachedSettings.toTimeConfig()
        val fairness = cachedSettings.toFairnessConfig(preset?.alphaOverride)
        val constraints = cachedSettings.toConstraintConfig()
        val tierOrder = cachedSettings.toTierOrder()
        val search = SearchConfig(seedOverride, cachedSettings.ilsIterations)
        val frozen = if (fullRegen) null else buildFrozenState()
        // 핀은 전체 재생성에서도 지켜져야 한다 — 생성 전에 직접 넣어둔 칸이 여기에 해당한다.
        // 슬롯 번호가 아니라 그 칸의 '시각'으로 넘긴다(브레이크를 옮기면 그리드가 다시 짜이므로).
        val slotTimes = lastOutput?.slots.orEmpty().associateBy { it.index }
        val pinned = lastOutput?.cells.orEmpty()
            .filter { it.isPinned }
            .mapNotNull { cell ->
                val slot = slotTimes[cell.slotIndex] ?: return@mapNotNull null
                PinnedAssignment(
                    memberId = cell.memberId,
                    startMin = slot.startMin,
                    endMin = slot.endMin,
                    positionId = cell.positionId,
                    isBreak = cell.state == CellState.BREAK
                )
            }
        return RotationInput(workers, breakSpans, positions, timeConfig, fairness, constraints, tierOrder, search, frozen, pinned)
    }

    private fun buildFrozenState(): FrozenState? {
        val output = lastOutput ?: return null
        val cursor = pendingCursorOverride ?: nowMinutes()
        val frozenSlots = output.slots.filter { it.endMin <= cursor }
        if (frozenSlots.isEmpty()) return null
        val frozenIndices = frozenSlots.map { it.index }.toSet()
        val frozenCells = output.cells.filter { it.slotIndex in frozenIndices }
        val lastIdx = frozenSlots.maxOf { it.index }
        val debtAtCursor = output.debtCurve.mapValues { (_, curve) -> curve.getOrNull(lastIdx) ?: 0.0 }
        return FrozenState(cursor, frozenSlots, frozenCells, debtAtCursor)
    }

    private fun applyOutput(output: RotationOutput) {
        val manuallyEditedKeys = output.cells.filter { it.isManuallyEdited }.map { it.slotIndex to it.memberId }.toSet()
        val displayedViolations = output.violations.filterNot { (it.slotIndex to it.memberId) in manuallyEditedKeys }
        val slotsUi = output.slots.sortedBy { it.index }
            .map { RotationSlotUi(it.index, it.startMin, it.endMin, it.type, it.isFrozen, it.remainderNote) }

        _uiState.update {
            it.copy(
                slots = slotsUi, rows = buildRows(slotsUi, output, displayedViolations), isDraft = !planGenerated,
                score = output.score, violations = displayedViolations,
                debtCurve = output.debtCurve, targetCurve = output.targetCurve, seed = output.seed
            )
        }
    }

    /**
     * 결과 표의 행을 만든다. [output]이 null이면 아직 생성 전(초안)이라 포지션 없이 근무/휴게만 칠한다.
     *
     * **행의 원본은 언제나 근무조(cachedRoles)다.** 예전에는 생성 후 경로만 결과 셀(output.cells)에서
     * 행을 뽑았는데, 그러면 배정이 비거나(자정 넘김·프리셋 교체) 멤버가 삭제되는 순간 그 줄이 표에서
     * 통째로 사라졌다. 근무조 하나당 줄 하나가 항상 나오고, 사람이 없으면 '미배정'으로 보이는 게 맞다.
     *
     * 근무/휴게 여부는 **지금 캐시된(라이브) 스케줄·브레이크**로 판정한다 — 재생성을 누르지 않고
     * 근무스케줄만 바꿔도 그 줄의 시간 표시와 근무/휴게 칸은 바로 갱신되어야 드롭이 먹었는지 알 수 있다.
     * 포지션 배치(누가 몇 시에 무슨 자리인지)만 마지막 생성 결과를 그대로 쓴다 — 이건 재생성을 눌러야 바뀐다(§10-6).
     */
    private fun buildRows(
        slotsUi: List<RotationSlotUi>,
        output: RotationOutput?,
        violations: List<Violation>
    ): List<RotationRowUi> {
        val positionById = cachedPositions.associateBy { it.id }
        val memberById = cachedMembers.associateBy { it.id }
        val violationKeys = violations.map { it.slotIndex to it.memberId }.toSet()
        val assignmentByLabel = cachedRoleAssignments.associateBy { it.roleLabel }
        val storedByMember = output?.cells.orEmpty().groupBy { it.memberId }

        // 정렬 기준은 근무조의 sortOrder다 — 출근시각으로 정렬하면 근무 스케줄 하나만 바꿔도
        // 그 사람의 출근시각이 바뀌면서 표 전체 행 순서가 재배열돼 버린다.
        return cachedRoles
            .filter { it.presetId == _uiState.value.selectedPresetId }
            .sortedWith(compareBy({ it.sortOrder }, { it.label }))
            .map { role ->
                val assignment = assignmentByLabel[role.label]
                val startMin = assignment?.startMin ?: role.startMin
                val endMin = assignment?.endMin ?: role.endMin
                // 멤버가 삭제된 뒤 남은 id는 미배정으로 취급한다 — 이름만 빈 채로 배정된 척하면 안 된다.
                val member = assignment?.memberId?.let { memberById[it] }
                val liveBreak = member?.let { m -> cachedBreaks.firstOrNull { it.memberId == m.id } }
                val stored = member?.let { m -> storedByMember[m.id]?.associateBy { it.slotIndex } }.orEmpty()

                val cells = slotsUi.map { slot ->
                    val working = member != null && slot.startMin < endMin && slot.endMin > startMin
                    val storedCell = stored[slot.index]
                    val liveBreakCovers = liveBreak != null &&
                        slot.startMin < liveBreak.endMin && slot.endMin > liveBreak.startMin
                    // 휴게 판정 우선순위: 손으로 고친 칸 > 핀 > 브레이크 시각(라이브).
                    // 손으로 포지션을 넣었으면 그 사람의 브레이크 시간대라도 포지션이 이겨야 한다 —
                    // 안 그러면 휴게 칸을 다른 포지션으로 바꿔도 즉시 휴게로 되돌아가 수정이 안 먹는다.
                    val onBreak = when {
                        storedCell?.isManuallyEdited == true -> storedCell.state == CellState.BREAK
                        storedCell?.state == CellState.BREAK && storedCell.isPinned -> true
                        else -> liveBreakCovers
                    }
                    when {
                        !working -> RotationCellUi(slot.index, CellState.OFF, null, null, null, isPinned = false, hasViolation = false)
                        onBreak -> RotationCellUi(slot.index, CellState.BREAK, null, null, null, isPinned = storedCell?.isPinned ?: false, hasViolation = false)
                        else -> {
                            val assigned = storedCell?.takeIf { it.state == CellState.ASSIGNED }
                            val position = assigned?.positionId?.let { positionById[it] }
                            RotationCellUi(
                                slotIndex = slot.index,
                                state = CellState.ASSIGNED,
                                positionId = assigned?.positionId,
                                positionName = position?.name,
                                colorHex = position?.colorHex,
                                isPinned = storedCell?.isPinned ?: false,
                                hasViolation = member != null && (slot.index to member.id) in violationKeys
                            )
                        }
                    }
                }
                RotationRowUi(
                    roleLabel = role.label,
                    memberId = member?.id,
                    name = member?.name ?: "",
                    colorHex = member?.colorHex ?: "",
                    startMin = startMin,
                    endMin = endMin,
                    scheduleLabel = assignment?.scheduleLabel,
                    isExcluded = assignment?.excludedFromAssign == true,
                    cells = cells
                )
            }
    }

    // ── 셀 조작 (§10-3) — 탭하면 포지션 직접 선택 다이얼로그가 뜨고, 고르면 그 자리를 강제로 대체한다 ──
    fun setCellPosition(slotIndex: Int, memberId: Long, positionId: Long?, isBreak: Boolean) {
        // 생성 전에도 직접 넣을 수 있어야 한다 — 알고리즘을 먼저 돌리라고 막아세우지 않는다.
        // 아직 결과가 없으면 근무/휴게만 채워진 빈 뼈대를 만들어 그 위에 얹는다.
        val startedFromScratch = lastOutput == null
        val output = lastOutput ?: buildSkeletonOutput()
        val cells = output.cells.toMutableList()
        val idx = cells.indexOfFirst { it.slotIndex == slotIndex && it.memberId == memberId }
        if (idx >= 0 && cells[idx].isPinned) return
        val newState = if (isBreak) CellState.BREAK else CellState.ASSIGNED
        val newPositionId = if (isBreak) null else positionId
        // 재생성 전에 근무시간을 늘린 구간은 결과에 아직 칸 자체가 없다. 예전엔 여기서 조용히 반환해
        // 눌러도 아무 일이 없었다 — 없으면 새로 만든다.
        val isNewCell = idx < 0
        // 생성 전에 직접 넣은 칸은 핀으로 고정한다 — 안 그러면 '포지션 배정하기'를 누르는 순간
        // 알고리즘이 덮어써서 먼저 넣은 의미가 사라진다(§7-1 4단계는 핀만 지켜준다).
        // 이미 잠긴 칸은 위에서 되돌려보냈으므로 여기서는 잠금 여부를 다시 볼 필요가 없다.
        if (isBreak) {
            // 한 사람의 휴게는 하나다. 다른 칸에 남아 있던 휴게(핀 포함)를 안 지우면 재생성할 때
            // 옛 자리의 휴게가 핀으로 되살아나 "옮겼는데 두 번 쉬는" 상태가 된다.
            for (i in cells.indices) {
                val other = cells[i]
                if (other.memberId == memberId && other.slotIndex != slotIndex && other.state == CellState.BREAK) {
                    cells[i] = other.copy(state = CellState.ASSIGNED, positionId = null, isPinned = false, isManuallyEdited = true)
                }
            }
        }
        if (isNewCell) {
            cells += Cell(slotIndex, memberId, newPositionId, newState, isPinned = startedFromScratch, isManuallyEdited = true)
        } else {
            cells[idx] = cells[idx].copy(
                positionId = newPositionId, state = newState,
                isPinned = startedFromScratch, isManuallyEdited = true
            )
        }
        val newOutput = rescored(output.copy(cells = cells))
        lastOutput = newOutput
        applyOutput(newOutput)
        val slot = output.slots.firstOrNull { it.index == slotIndex }
        viewModelScope.launch {
            // 표에서 바꾼 휴게는 그 사람의 '브레이크 시각' 자체를 옮긴다. 칸 상태만 바꾸면 원본 시각이
            // 그대로 남아 재생성할 때 예전 자리에 휴게가 되살아난다 — 표 편집이 무의미해지는 원인이었다.
            if (slot != null) {
                if (isBreak) moveBreakToSlot(memberId, slot.startMin, slot.endMin)
                else clearBreakOverSlot(memberId, slot.startMin, slot.endMin)
            }
            val planId = currentPlanId
            // 새로 만든 칸·새 뼈대는 UPDATE 문이 건드릴 행이 없어 저장이 조용히 누락된다 — 통째로 다시 저장한다.
            if (isNewCell || startedFromScratch || planId == null) {
                val dpId = requireDayPlanId() ?: return@launch
                persistPlan(dpId, newOutput)
            } else {
                repository.updateCellPositionAndState(planId, slotIndex, memberId, newPositionId, newState.name)
            }
        }
    }


    fun togglePin(slotIndex: Int, memberId: Long) {
        // setCellPosition과 같은 이유로, 생성 전에도 잠글 수 있어야 한다(휴게를 미리 못박아 두는 경우 등).
        val startedFromScratch = lastOutput == null
        val output = lastOutput ?: buildSkeletonOutput()
        val cells = output.cells.toMutableList()
        val idx = cells.indexOfFirst { it.slotIndex == slotIndex && it.memberId == memberId }
        if (idx < 0) return
        val pinned = !cells[idx].isPinned
        // 브레이크 하나가 여러 칸에 걸쳐 있으면 표에는 한 덩어리로 보이고 자물쇠도 하나만 나온다 —
        // 잠글 때 이어진 칸을 같이 잠가야 재생성에서 브레이크가 반쪽만 남지 않는다.
        val isBreakRun = cells[idx].state == CellState.BREAK
        cells[idx] = cells[idx].copy(isPinned = pinned)
        if (isBreakRun) {
            val slotOrder = output.slots.sortedBy { it.index }.map { it.index }
            val here = slotOrder.indexOf(slotIndex)
            if (here >= 0) {
                val byIndex = cells.withIndex()
                    .filter { it.value.memberId == memberId }
                    .associate { it.value.slotIndex to it.index }
                for (step in listOf(-1, 1)) {
                    var k = here + step
                    while (k in slotOrder.indices) {
                        val cellIdx = byIndex[slotOrder[k]] ?: break
                        if (cells[cellIdx].state != CellState.BREAK) break
                        cells[cellIdx] = cells[cellIdx].copy(isPinned = pinned)
                        k += step
                    }
                }
            }
        }
        val newOutput = output.copy(cells = cells)
        lastOutput = newOutput
        applyOutput(newOutput)
        viewModelScope.launch {
            val planId = currentPlanId
            // 뼈대를 방금 만들었거나 이어진 휴게 칸을 한 번에 잠갔으면 단일 UPDATE로는 부족하다 — 통째로 저장한다.
            if (startedFromScratch || planId == null || isBreakRun) {
                val dpId = requireDayPlanId() ?: return@launch
                persistPlan(dpId, newOutput)
            } else {
                repository.updateCellPinned(planId, slotIndex, memberId, pinned)
            }
        }
    }

    /**
     * 표의 이름 칸에 멤버를 드롭했을 때. 근무 슬롯(roleLabel)의 담당자를 바꾸고, 다른 행에서 끌어온
     * 것이면 서로 맞바꾼다. 이미 만들어진 표가 있고 두 사람 다 표에 있으면(맞바꿈) 셀 이름표만
     * 바꿔 끼워 시간·포지션을 그대로 유지한다. 그 외(새 사람 배정 등)는 데이터만 저장하고 표는
     * 그대로 둔다 — 사용자가 재생성 버튼을 눌러야 반영된다(§10-6).
     */
    fun dropMemberOnRow(targetRoleLabel: String, sourceRoleLabel: String?, memberId: Long) = viewModelScope.launch {
        val dpId = requireDayPlanId() ?: return@launch
        // 근무조는 있는데 오늘 배정 행이 아직 없으면 여기서 만들어 준다 — 없으면 아래 map이 아무것도
        // 못 바꿔서 드래그가 조용히 씹힌다.
        ensureAssignmentRows(dpId)
        val targetPrevMemberId = cachedRoleAssignments.firstOrNull { it.roleLabel == targetRoleLabel }?.memberId
        if (targetPrevMemberId == memberId) return@launch

        cachedRoleAssignments = cachedRoleAssignments.map { ra ->
            when (ra.roleLabel) {
                targetRoleLabel -> ra.copy(memberId = memberId)
                sourceRoleLabel -> ra.copy(memberId = targetPrevMemberId)
                else -> ra
            }
        }

        val output = lastOutput
        // 두 사람을 맞바꾼 경우에만 셀 이름표 교체로 끝낼 수 있다(둘 다 이미 표에 있으므로).
        val isPureSwap = output != null && sourceRoleLabel != null && targetPrevMemberId != null &&
            output.cells.any { it.memberId == memberId } && output.cells.any { it.memberId == targetPrevMemberId }

        if (output != null && isPureSwap) {
            val other = targetPrevMemberId!!
            val cells = output.cells.map { cell ->
                when (cell.memberId) {
                    memberId -> cell.copy(memberId = other)
                    other -> cell.copy(memberId = memberId)
                    else -> cell
                }
            }
            cachedBreaks = cachedBreaks.map { b ->
                when (b.memberId) {
                    memberId -> b.copy(memberId = other)
                    other -> b.copy(memberId = memberId)
                    else -> b
                }
            }
            val newOutput = rescored(output.copy(cells = cells))
            lastOutput = newOutput
            applyOutput(newOutput)
            syncFromCache()
            val planId = currentPlanId
            repository.saveRoleAssignments(dpId, cachedRoleAssignments)
            repository.saveBreaks(dpId, cachedBreaks)
            if (planId != null) repository.swapCellMembers(planId, memberId, other)
            return@launch
        }

        syncFromCache()
        repository.saveRoleAssignments(dpId, cachedRoleAssignments)
        // 여기서 자동으로 재계산하지 않는다 — applyTemplateToRow와 동일한 원칙(§10-6): 앵커가 바뀔 때마다
        // 자동으로 전체 재생성을 돌리면 매번 표 전체가 다시 섞여 순서·배치가 지멋대로 바뀐 것처럼 보인다.
        // 새 사람이 들어오거나 빠지면 그 사람의 근무시간이 새 앵커가 되어 슬롯 번호가 통째로 밀리므로,
        // 사용자가 '전체 재생성' / '특정 시간부터 재생성'을 직접 눌렀을 때만 반영한다.
    }

    /**
     * 근무 스케줄 템플릿을 행에 드롭 — 그 행의 오늘 출퇴근 시각을 바꾸고, 템플릿의 기본 브레이크도 함께 넣는다.
     * 결과 표는 여기서 바로 다시 짜지 않는다 — 앵커(출퇴근·브레이크)가 바뀔 때마다 자동으로 전체 재생성을
     * 돌리면 매번 표 전체가 다시 섞여 순서·배치가 지멋대로 바뀌는 것처럼 보인다. 사용자가 '전체 재생성' /
     * '특정 시간부터 재생성'을 직접 눌렀을 때만 반영되도록 데이터만 바꿔두고 멈춘다.
     */
    fun applyTemplateToRow(roleLabel: String, template: RotationScheduleTemplateUi) = viewModelScope.launch {
        // 두 갱신을 한 코루틴에서 순서대로 처리한다 — 따로 띄우면 시각 반영 전에 담당자를 읽어
        // 브레이크가 엉뚱한 사람에게 들어가거나 통째로 누락될 수 있다.
        updateRoleTimeInternal(roleLabel, template.startMin, template.endMin, template.label)
        val memberId = cachedRoleAssignments.firstOrNull { it.roleLabel == roleLabel }?.memberId
        if (memberId != null && template.breakMinutes > 0) {
            val breakStart = template.breakStartMin ?: template.startMin
            updateBreakInternal(memberId, breakStart, breakStart + template.breakMinutes)
        }
    }

    /**
     * 결과표를 통째로 저장하고 planId를 갱신한다. 얼림 커서는 **마지막 생성 때 쓴 값**을 그대로 이어 쓴다 —
     * 칸을 손으로 고쳤다고 해서 이미 지나간 구간의 기준 시각이 달라지면 안 된다.
     */
    private suspend fun persistPlan(dayPlanId: Long, output: RotationOutput) {
        val cursor = lastInput?.frozen?.freezeCursorMin ?: cachedSettings.windowStart
        currentPlanId = repository.savePlan(dayPlanId, cursor, output)
    }

    /** 결과표가 더 이상 유효하지 않을 때(날짜·프리셋 교체, 초기화) 들고 있던 계산 상태를 한꺼번에 버린다. */
    private fun clearPlanState() {
        lastOutput = null
        lastInput = null
        currentPlanId = null
        planGenerated = false
    }

    private fun rescored(output: RotationOutput): RotationOutput {
        val input = lastInput ?: return output
        val result = RotationScore.evaluate(input, output.slots, output.cells)
        return output.copy(score = result.score, violations = result.violations, debtCurve = result.debtCurve, targetCurve = result.targetCurve)
    }

    private fun monthKeyOf(date: LocalDate): String = "s" + date.format(SCHEDULE_MONTH_FORMAT)

    private fun nowMinutes(): Int = LocalTime.now().let { it.hour * 60 + it.minute }
    private fun today(): String = LocalDate.now().toString()
}

// ═══════════════════ Entity ↔ UI 모델 변환 ═══════════════════

private fun RotationPositionEntity.toUi() = RotationPositionUi(
    id = id, name = name, isHigh = intensity == "HIGH", minCount = minCount, maxCount = maxCount,
    openPriority = openPriority, overflowPriority = overflowPriority, isActive = isActive,
    colorHex = colorHex, sortOrder = sortOrder
)

private fun RotationPositionUi.toEntity() = RotationPositionEntity(
    id = id, name = name, intensity = if (isHigh) "HIGH" else "LOW", minCount = minCount, maxCount = maxCount,
    openPriority = openPriority, overflowPriority = overflowPriority, isActive = isActive,
    colorHex = colorHex, sortOrder = sortOrder
)

private fun ScheduleTemplateEntity.toUi() = RotationScheduleTemplateUi(id, label, startMin, endMin, breakStartMin, breakMinutes, sortOrder)

private fun RotationSettingsEntity.toUi() = RotationSettingsUi(
    windowStart = windowStart, windowEnd = windowEnd, preBreakDesiredMinutes = preBreakDesiredMinutes,
    inBreakDesiredMinutes = inBreakDesiredMinutes, postBreakDesiredMinutes = postBreakDesiredMinutes,
    minSlotMinutes = minSlotMinutes, defaultBreakStartMin = defaultBreakStartMin,
    defaultShiftDurationMinutes = defaultShiftDurationMinutes,
    remainderPolicy = RemainderPolicy.valueOf(remainderPolicy),
    handoverMode = HandoverMode.valueOf(handoverMode),
    handoverMinutes = handoverMinutes, handoverMinSlotMinutes = handoverMinSlotMinutes, alpha = alpha,
    targetBasis = TargetBasis.valueOf(targetBasis),
    fairnessPriority = FairnessPriority.valueOf(fairnessPriority),
    midBandMinutes = midBandMinutes, samePositionMaxRun = samePositionMaxRun, breakInterruptsRun = breakInterruptsRun,
    allowHighChain = allowHighChain, highMaxRun = highMaxRun, highCooldownSlots = highCooldownSlots,
    relaxPreBreak = relaxPreBreak,
    tierOrder = tierOrderCsv.split(",").filter { it.isNotBlank() }.map { Tier.valueOf(it) },
    ilsIterations = ilsIterations
)

private fun RotationSettingsUi.toEntity() = RotationSettingsEntity(
    windowStart = windowStart, windowEnd = windowEnd, preBreakDesiredMinutes = preBreakDesiredMinutes,
    inBreakDesiredMinutes = inBreakDesiredMinutes, postBreakDesiredMinutes = postBreakDesiredMinutes,
    minSlotMinutes = minSlotMinutes, defaultBreakStartMin = defaultBreakStartMin,
    defaultShiftDurationMinutes = defaultShiftDurationMinutes,
    remainderPolicy = remainderPolicy.name, handoverMode = handoverMode.name,
    handoverMinutes = handoverMinutes, handoverMinSlotMinutes = handoverMinSlotMinutes,
    alpha = alpha, targetBasis = targetBasis.name, fairnessPriority = fairnessPriority.name,
    midBandMinutes = midBandMinutes, samePositionMaxRun = samePositionMaxRun, breakInterruptsRun = breakInterruptsRun,
    allowHighChain = allowHighChain, highMaxRun = highMaxRun, highCooldownSlots = highCooldownSlots,
    relaxPreBreak = relaxPreBreak, tierOrderCsv = tierOrder.joinToString(",") { it.name }, ilsIterations = ilsIterations
)
