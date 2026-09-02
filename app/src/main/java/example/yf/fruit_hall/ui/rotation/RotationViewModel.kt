package example.yf.fruit_hall.ui.rotation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.core.rotation.BreakSpan
import example.yf.fruit_hall.core.rotation.CellState
import example.yf.fruit_hall.core.rotation.FairnessPriority
import example.yf.fruit_hall.core.rotation.FrozenState
import example.yf.fruit_hall.core.rotation.HandoverMode
import example.yf.fruit_hall.core.rotation.RemainderPolicy
import example.yf.fruit_hall.core.rotation.RotationInput
import example.yf.fruit_hall.core.rotation.RotationOutput
import example.yf.fruit_hall.core.rotation.RotationScore
import example.yf.fruit_hall.core.rotation.SearchConfig
import example.yf.fruit_hall.core.rotation.TargetBasis
import example.yf.fruit_hall.core.rotation.Tier
import example.yf.fruit_hall.core.rotation.Worker
import example.yf.fruit_hall.data.position.PositionRepository
import example.yf.fruit_hall.data.position.entity.MemberEntity
import example.yf.fruit_hall.data.rotation.RotationRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

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
    private val planRotation: PlanRotationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RotationUiState(date = today()))
    val uiState: StateFlow<RotationUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var dayPlanId: Long? = null
    private var currentPlanId: Long? = null
    private var lastOutput: RotationOutput? = null
    private var lastInput: RotationInput? = null

    private var cachedMembers: List<MemberEntity> = emptyList()
    private var cachedPositions: List<RotationPositionEntity> = emptyList()
    private var cachedPresets: List<ShiftPresetEntity> = emptyList()
    private var cachedRoles: List<ShiftRoleEntity> = emptyList()
    private var cachedSettings: RotationSettingsEntity = RotationSettingsEntity()
    private var cachedRoleAssignments: List<DayRoleAssignmentEntity> = emptyList()
    private var cachedBreaks: List<BreakAssignmentEntity> = emptyList()
    private var cachedScheduleTemplates: List<ScheduleTemplateEntity> = emptyList()

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
    }

    private suspend fun ensureDayPlan(presetId: Long) {
        val date = today()
        val dayPlan = repository.getOrCreateDayPlan(date, presetId, System.nanoTime(), "")
        dayPlanId = dayPlan.id

        val rolesForPreset = cachedRoles.filter { it.presetId == presetId && it.isActive }
        val existingByLabel = repository.getRoleAssignments(dayPlan.id).associateBy { it.roleLabel }
        val merged = rolesForPreset.map { role ->
            val prev = existingByLabel[role.label]
            DayRoleAssignmentEntity(
                id = prev?.id ?: 0,
                dayPlanId = dayPlan.id,
                roleLabel = role.label,
                memberId = prev?.memberId,
                startMin = prev?.startMin ?: role.startMin,
                endMin = prev?.endMin ?: role.endMin
            )
        }
        repository.saveRoleAssignments(dayPlan.id, merged)
        cachedRoleAssignments = merged
        cachedBreaks = repository.getBreaks(dayPlan.id)

        _uiState.update { it.copy(selectedPresetId = presetId) }

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
            applyOutput(lastOutput!!)
        }
    }

    private fun syncFromCache() {
        _uiState.update { current ->
            current.copy(
                members = cachedMembers.map { MemberUi(it.id, it.name, it.colorHex) },
                positions = cachedPositions.map { it.toUi() },
                presets = cachedPresets.map { RotationPresetUi(it.id, it.name, it.isDefault, it.alphaOverride) },
                roles = cachedRoles.filter { it.presetId == current.selectedPresetId }.map { it.toUi() },
                scheduleTemplates = cachedScheduleTemplates.map { it.toUi() },
                settings = cachedSettings.toUi(),
                roleAssignments = buildRoleAssignmentUi(),
                breaks = buildBreakUi()
            )
        }
    }

    private fun buildRoleAssignmentUi(): List<RoleAssignmentUi> = cachedRoleAssignments.map { ra ->
        RoleAssignmentUi(
            roleLabel = ra.roleLabel,
            memberId = ra.memberId,
            memberName = cachedMembers.firstOrNull { it.id == ra.memberId }?.name,
            startMin = ra.startMin,
            endMin = ra.endMin
        )
    }

    private fun buildBreakUi(): List<BreakUi> = cachedBreaks.map { b ->
        BreakUi(
            memberId = b.memberId,
            memberName = cachedMembers.firstOrNull { it.id == b.memberId }?.name ?: "?",
            startMin = b.startMin,
            endMin = b.endMin,
            isManual = b.isManual
        )
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
    fun setDefaultPreset(id: Long) = viewModelScope.launch { repository.setDefaultPreset(id) }

    fun saveRole(role: RotationRoleUi) = viewModelScope.launch {
        repository.upsertRole(
            ShiftRoleEntity(
                id = role.id, presetId = role.presetId, label = role.label,
                startMin = role.startMin, endMin = role.endMin,
                breakOrder = role.breakOrder, breakMinutes = role.breakMinutes, sortOrder = role.sortOrder,
                isActive = role.isActive
            )
        )
        if (role.presetId == _uiState.value.selectedPresetId) ensureDayPlan(role.presetId)
    }
    fun deleteRole(role: RotationRoleUi) = viewModelScope.launch {
        repository.deleteRole(
            ShiftRoleEntity(role.id, role.presetId, role.label, role.startMin, role.endMin, role.breakOrder, role.breakMinutes, role.sortOrder, role.isActive)
        )
        if (role.presetId == _uiState.value.selectedPresetId) ensureDayPlan(role.presetId)
    }
    fun setRoleActive(id: Long, isActive: Boolean) = viewModelScope.launch {
        repository.setRoleActive(id, isActive)
        val presetId = _uiState.value.selectedPresetId
        if (presetId != null) ensureDayPlan(presetId)
    }

    // ── 근무 스케줄 템플릿 (프리셋 무관, 재사용) ──
    fun saveScheduleTemplate(template: RotationScheduleTemplateUi) = viewModelScope.launch {
        repository.upsertScheduleTemplate(
            ScheduleTemplateEntity(
                id = template.id, label = template.label, startMin = template.startMin, endMin = template.endMin,
                breakOrder = template.breakOrder, breakMinutes = template.breakMinutes, sortOrder = template.sortOrder
            )
        )
    }
    fun deleteScheduleTemplate(template: RotationScheduleTemplateUi) = viewModelScope.launch {
        repository.deleteScheduleTemplate(
            ScheduleTemplateEntity(template.id, template.label, template.startMin, template.endMin, template.breakOrder, template.breakMinutes, template.sortOrder)
        )
    }

    // ── 멤버 (position 탭과 데이터 공유 — 여기서 직접 추가/삭제 가능) ──
    fun addMember(name: String, colorHex: String) = viewModelScope.launch {
        positionRepository.addMember(MemberEntity(name = name, colorHex = colorHex))
    }
    fun deleteMember(id: Long) = viewModelScope.launch { positionRepository.deleteMember(id) }

    // ── 오늘 인원배정 (§3-3) ──
    fun assignMember(roleLabel: String, memberId: Long?) {
        val dpId = dayPlanId ?: return
        val updated = cachedRoleAssignments.map { if (it.roleLabel == roleLabel) it.copy(memberId = memberId) else it }
        cachedRoleAssignments = updated
        syncFromCache()
        viewModelScope.launch { repository.saveRoleAssignments(dpId, updated) }
    }

    fun updateRoleTime(roleLabel: String, startMin: Int, endMin: Int) {
        val dpId = dayPlanId ?: return
        val updated = cachedRoleAssignments.map { if (it.roleLabel == roleLabel) it.copy(startMin = startMin, endMin = endMin) else it }
        cachedRoleAssignments = updated
        syncFromCache()
        viewModelScope.launch { repository.saveRoleAssignments(dpId, updated) }
    }

    // ── 브레이크 (§3-4) ──
    fun generateDefaultBreaks() {
        val dpId = dayPlanId ?: return
        val presetId = _uiState.value.selectedPresetId
        val rolesWithBreak = cachedRoles.filter { it.presetId == presetId && it.breakOrder != null }.sortedBy { it.breakOrder }
        val assignedByLabel = cachedRoleAssignments.associateBy { it.roleLabel }
        var cursor = cachedSettings.defaultBreakStartMin
        val newBreaks = mutableListOf<BreakAssignmentEntity>()
        for (role in rolesWithBreak) {
            val memberId = assignedByLabel[role.label]?.memberId ?: continue
            newBreaks += BreakAssignmentEntity(dayPlanId = dpId, memberId = memberId, startMin = cursor, endMin = cursor + role.breakMinutes)
            cursor += role.breakMinutes
        }
        cachedBreaks = newBreaks
        syncFromCache()
        viewModelScope.launch { repository.saveBreaks(dpId, newBreaks) }
    }

    fun updateBreak(memberId: Long, startMin: Int, endMin: Int) {
        val dpId = dayPlanId ?: return
        val exists = cachedBreaks.any { it.memberId == memberId }
        val updated = if (exists) {
            cachedBreaks.map { if (it.memberId == memberId) it.copy(startMin = startMin, endMin = endMin, isManual = true) else it }
        } else {
            cachedBreaks + BreakAssignmentEntity(dayPlanId = dpId, memberId = memberId, startMin = startMin, endMin = endMin, isManual = true)
        }
        cachedBreaks = updated
        syncFromCache()
        viewModelScope.launch { repository.saveBreaks(dpId, updated) }
    }

    fun removeBreak(memberId: Long) {
        val dpId = dayPlanId ?: return
        val updated = cachedBreaks.filterNot { it.memberId == memberId }
        cachedBreaks = updated
        syncFromCache()
        viewModelScope.launch { repository.saveBreaks(dpId, updated) }
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
        showPositionManage: Boolean? = null,
        showPresetManage: Boolean? = null,
        showScheduleManage: Boolean? = null,
        showMemberAssign: Boolean? = null,
        showBreakEdit: Boolean? = null,
        showSettings: Boolean? = null
    ) {
        _uiState.update {
            it.copy(
                showMemberManage = showMemberManage ?: it.showMemberManage,
                showPositionManage = showPositionManage ?: it.showPositionManage,
                showPresetManage = showPresetManage ?: it.showPresetManage,
                showScheduleManage = showScheduleManage ?: it.showScheduleManage,
                showMemberAssign = showMemberAssign ?: it.showMemberAssign,
                showBreakEdit = showBreakEdit ?: it.showBreakEdit,
                showSettings = showSettings ?: it.showSettings
            )
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    // ── 생성 (§7) ──
    fun regenerateFull() = generate(fullRegen = true)
    fun regenerateFromCursor() = generate(fullRegen = false)

    private fun generate(fullRegen: Boolean) {
        val dpId = dayPlanId ?: return
        if (cachedRoleAssignments.none { it.memberId != null }) {
            _uiState.update { it.copy(errorMessage = "인원배정을 먼저 해주세요") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, errorMessage = null) }
            try {
                val seed = if (fullRegen) System.nanoTime() else (lastOutput?.seed ?: System.nanoTime())
                val input = buildInput(fullRegen, seed)
                lastInput = input
                val output = planRotation(input)
                lastOutput = output
                val cursor = input.frozen?.freezeCursorMin ?: input.timeConfig.windowStart
                currentPlanId = repository.savePlan(dpId, cursor, output)
                applyOutput(output)
                _uiState.update { it.copy(showMemberAssign = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "생성에 실패했습니다") }
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    private fun buildInput(fullRegen: Boolean, seedOverride: Long): RotationInput {
        val workers = cachedRoleAssignments.filter { it.memberId != null }
            .map { Worker(it.memberId!!, it.startMin, it.endMin) }
        val breakSpans = cachedBreaks.map { BreakSpan(it.memberId, it.startMin, it.endMin) }
        val positions = cachedPositions.map { it.toDomain() }
        val preset = cachedPresets.firstOrNull { it.id == _uiState.value.selectedPresetId }
        val timeConfig = cachedSettings.toTimeConfig()
        val fairness = cachedSettings.toFairnessConfig(preset?.alphaOverride)
        val constraints = cachedSettings.toConstraintConfig()
        val tierOrder = cachedSettings.toTierOrder()
        val search = SearchConfig(seedOverride, cachedSettings.ilsIterations)
        val frozen = if (fullRegen) null else buildFrozenState()
        return RotationInput(workers, breakSpans, positions, timeConfig, fairness, constraints, tierOrder, search, frozen)
    }

    private fun buildFrozenState(): FrozenState? {
        val output = lastOutput ?: return null
        val cursor = nowMinutes()
        val frozenSlots = output.slots.filter { it.endMin <= cursor }
        if (frozenSlots.isEmpty()) return null
        val frozenIndices = frozenSlots.map { it.index }.toSet()
        val frozenCells = output.cells.filter { it.slotIndex in frozenIndices }
        val lastIdx = frozenSlots.maxOf { it.index }
        val debtAtCursor = output.debtCurve.mapValues { (_, curve) -> curve.getOrNull(lastIdx) ?: 0.0 }
        return FrozenState(cursor, frozenSlots, frozenCells, debtAtCursor)
    }

    private fun applyOutput(output: RotationOutput) {
        val positionById = cachedPositions.associateBy { it.id }
        val memberById = cachedMembers.associateBy { it.id }
        val violationKeys = output.violations.map { it.slotIndex to it.memberId }.toSet()
        val slotsUi = output.slots.sortedBy { it.index }
            .map { RotationSlotUi(it.index, it.startMin, it.endMin, it.type, it.isFrozen, it.remainderNote) }

        val memberIds = output.cells.map { it.memberId }.distinct()
        val rows = memberIds.mapNotNull { mid ->
            val member = memberById[mid] ?: return@mapNotNull null
            val cellsForMember = output.cells.filter { it.memberId == mid }.associateBy { it.slotIndex }
            val cellUis = slotsUi.map { slot ->
                val cell = cellsForMember[slot.index]
                val position = cell?.positionId?.let { positionById[it] }
                RotationCellUi(
                    slotIndex = slot.index,
                    state = cell?.state ?: CellState.OFF,
                    positionId = cell?.positionId,
                    positionName = position?.name,
                    colorHex = position?.colorHex,
                    isPinned = cell?.isPinned ?: false,
                    hasViolation = (slot.index to mid) in violationKeys
                )
            }
            RotationRowUi(mid, member.name, member.colorHex, cellUis)
        }.sortedBy { it.name }

        _uiState.update {
            it.copy(
                slots = slotsUi, rows = rows, score = output.score, violations = output.violations,
                debtCurve = output.debtCurve, targetCurve = output.targetCurve, seed = output.seed
            )
        }
    }

    // ── 셀 조작 (§10-3) — 탭으로 두 사람을 골라 같은 슬롯에서 교환 ──
    fun tapCell(slotIndex: Int, memberId: Long) {
        val current = _uiState.value.selectedCell
        when {
            current == null -> _uiState.update { it.copy(selectedCell = SelectedCell(slotIndex, memberId)) }
            current.slotIndex == slotIndex && current.memberId != memberId -> {
                swapCells(slotIndex, current.memberId, memberId)
                _uiState.update { it.copy(selectedCell = null) }
            }
            else -> _uiState.update { it.copy(selectedCell = null) }
        }
    }

    private fun swapCells(slotIndex: Int, memberA: Long, memberB: Long) {
        val output = lastOutput ?: return
        val cells = output.cells.toMutableList()
        val idxA = cells.indexOfFirst { it.slotIndex == slotIndex && it.memberId == memberA }
        val idxB = cells.indexOfFirst { it.slotIndex == slotIndex && it.memberId == memberB }
        if (idxA < 0 || idxB < 0) return
        val a = cells[idxA]
        val b = cells[idxB]
        if (a.state != CellState.ASSIGNED || b.state != CellState.ASSIGNED || a.isPinned || b.isPinned) return
        cells[idxA] = a.copy(positionId = b.positionId, isManuallyEdited = true)
        cells[idxB] = b.copy(positionId = a.positionId, isManuallyEdited = true)
        val newOutput = rescored(output.copy(cells = cells))
        lastOutput = newOutput
        applyOutput(newOutput)
        val planId = currentPlanId ?: return
        viewModelScope.launch {
            repository.updateCellPosition(planId, slotIndex, memberA, cells[idxA].positionId)
            repository.updateCellPosition(planId, slotIndex, memberB, cells[idxB].positionId)
        }
    }

    fun togglePin(slotIndex: Int, memberId: Long) {
        val output = lastOutput ?: return
        val cells = output.cells.toMutableList()
        val idx = cells.indexOfFirst { it.slotIndex == slotIndex && it.memberId == memberId }
        if (idx < 0) return
        cells[idx] = cells[idx].copy(isPinned = !cells[idx].isPinned)
        val newOutput = output.copy(cells = cells)
        lastOutput = newOutput
        applyOutput(newOutput)
        val planId = currentPlanId ?: return
        viewModelScope.launch { repository.updateCellPinned(planId, slotIndex, memberId, cells[idx].isPinned) }
    }

    private fun rescored(output: RotationOutput): RotationOutput {
        val input = lastInput ?: return output
        val result = RotationScore.evaluate(input, output.slots, output.cells)
        return output.copy(score = result.score, violations = result.violations, debtCurve = result.debtCurve, targetCurve = result.targetCurve)
    }

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

private fun ShiftRoleEntity.toUi() = RotationRoleUi(id, presetId, label, startMin, endMin, breakOrder, breakMinutes, sortOrder, isActive)

private fun ScheduleTemplateEntity.toUi() = RotationScheduleTemplateUi(id, label, startMin, endMin, breakOrder, breakMinutes, sortOrder)

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
