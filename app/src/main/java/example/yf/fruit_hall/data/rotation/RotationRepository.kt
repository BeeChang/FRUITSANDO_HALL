package example.yf.fruit_hall.data.rotation

import example.yf.fruit_hall.core.rotation.BreakSpan
import example.yf.fruit_hall.core.rotation.Cell
import example.yf.fruit_hall.core.rotation.CellState
import example.yf.fruit_hall.core.rotation.ConstraintConfig
import example.yf.fruit_hall.core.rotation.FairnessConfig
import example.yf.fruit_hall.core.rotation.FairnessPriority
import example.yf.fruit_hall.core.rotation.HandoverMode
import example.yf.fruit_hall.core.rotation.Intensity
import example.yf.fruit_hall.core.rotation.RemainderPolicy
import example.yf.fruit_hall.core.rotation.RotPosition
import example.yf.fruit_hall.core.rotation.RotationOutput
import example.yf.fruit_hall.core.rotation.SegmentType
import example.yf.fruit_hall.core.rotation.Slot
import example.yf.fruit_hall.core.rotation.TargetBasis
import example.yf.fruit_hall.core.rotation.TimeConfig
import example.yf.fruit_hall.core.rotation.Tier
import example.yf.fruit_hall.data.rotation.dao.DayPlanDao
import example.yf.fruit_hall.data.rotation.dao.RotationPlanDao
import example.yf.fruit_hall.data.rotation.dao.RotationPositionDao
import example.yf.fruit_hall.data.rotation.dao.RotationSettingsDao
import example.yf.fruit_hall.data.rotation.dao.ScheduleTemplateDao
import example.yf.fruit_hall.data.rotation.dao.ShiftPresetDao
import example.yf.fruit_hall.data.rotation.entity.BreakAssignmentEntity
import example.yf.fruit_hall.data.rotation.entity.DayPlanEntity
import example.yf.fruit_hall.data.rotation.entity.DayRoleAssignmentEntity
import example.yf.fruit_hall.data.rotation.entity.RotationCellEntity
import example.yf.fruit_hall.data.rotation.entity.RotationPlanEntity
import example.yf.fruit_hall.data.rotation.entity.RotationPositionEntity
import example.yf.fruit_hall.data.rotation.entity.RotationSettingsEntity
import example.yf.fruit_hall.data.rotation.entity.RotationSlotEntity
import example.yf.fruit_hall.data.rotation.entity.ScheduleTemplateEntity
import example.yf.fruit_hall.data.rotation.entity.ShiftPresetEntity
import example.yf.fruit_hall.data.rotation.entity.ShiftRoleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class LoadedPlan(val planId: Long, val seed: Long, val slots: List<Slot>, val cells: List<Cell>)

@Singleton
class RotationRepository @Inject constructor(
    private val positionDao: RotationPositionDao,
    private val presetDao: ShiftPresetDao,
    private val dayPlanDao: DayPlanDao,
    private val planDao: RotationPlanDao,
    private val settingsDao: RotationSettingsDao,
    private val scheduleTemplateDao: ScheduleTemplateDao
) {
    // ── 포지션 (§3-1) ──
    fun observePositions(): Flow<List<RotationPositionEntity>> = positionDao.observeAll()
    suspend fun upsertPosition(position: RotationPositionEntity): Long = positionDao.upsert(position)
    suspend fun deletePosition(position: RotationPositionEntity) = positionDao.delete(position)
    suspend fun setPositionActive(id: Long, isActive: Boolean) = positionDao.updateActive(id, isActive)

    // ── 근무조 프리셋 (§3-2) ──
    fun observePresets(): Flow<List<ShiftPresetEntity>> = presetDao.observePresets()
    fun observeRoles(): Flow<List<ShiftRoleEntity>> = presetDao.observeRoles()
    suspend fun upsertPreset(preset: ShiftPresetEntity): Long = presetDao.upsertPreset(preset)
    suspend fun deletePreset(preset: ShiftPresetEntity) = presetDao.deletePreset(preset)
    suspend fun upsertRole(role: ShiftRoleEntity): Long = presetDao.upsertRole(role)
    suspend fun deleteRole(role: ShiftRoleEntity) = presetDao.deleteRole(role)
    suspend fun setDefaultPreset(id: Long) {
        presetDao.clearDefault()
        presetDao.setDefault(id)
    }

    // ── 근무 스케줄 템플릿 (여러 프리셋에서 재사용) ──
    fun observeScheduleTemplates(): Flow<List<ScheduleTemplateEntity>> = scheduleTemplateDao.observeAll()
    suspend fun upsertScheduleTemplate(template: ScheduleTemplateEntity): Long = scheduleTemplateDao.upsert(template)
    suspend fun deleteScheduleTemplate(template: ScheduleTemplateEntity) = scheduleTemplateDao.delete(template)

    // ── 설정 (§10-7) ──
    fun observeSettings(): Flow<RotationSettingsEntity> = settingsDao.observe().map { it ?: RotationSettingsEntity() }
    suspend fun updateSettings(settings: RotationSettingsEntity) = settingsDao.upsert(settings)

    // ── 그날의 계획 (§3-3, §3-4) ──
    suspend fun getDayPlan(date: String): DayPlanEntity? = dayPlanDao.getByDate(date)

    suspend fun getOrCreateDayPlan(date: String, presetId: Long, seed: Long, settingsSnapshot: String): DayPlanEntity {
        dayPlanDao.getByDate(date)?.let { existing ->
            if (existing.presetId == presetId) return existing
            val updated = existing.copy(presetId = presetId) // 같은 날 프리셋을 바꾼 경우 갱신
            dayPlanDao.upsertDayPlan(updated)
            return updated
        }
        val id = dayPlanDao.upsertDayPlan(DayPlanEntity(date = date, presetId = presetId, seed = seed, settingsSnapshot = settingsSnapshot))
        return dayPlanDao.getByDate(date) ?: DayPlanEntity(id, date, presetId, seed, settingsSnapshot)
    }

    suspend fun getRoleAssignments(dayPlanId: Long): List<DayRoleAssignmentEntity> = dayPlanDao.getRoleAssignments(dayPlanId)

    // ⚠ 저장은 "이 dayPlan의 행을 전부 지우고 다시 넣기"다. 그래서 넘어온 엔티티의 id·dayPlanId를
    // 그대로 믿으면 두 가지가 터진다:
    //  1) 호출자 캐시에 남아 있던 다른 날(어제)·다른 프리셋의 dayPlanId가 그대로 들어가 오늘 표에서 사라진다.
    //  2) id는 테이블 전역 자동증가라, 남아 있던 옛 id로 REPLACE 삽입하면 **다른 날의 행을 덮어쓴다.**
    // 방금 통째로 지웠으니 id는 의미가 없다 — id=0으로 새로 받고 dayPlanId는 인자 값으로 강제한다.
    suspend fun saveRoleAssignments(dayPlanId: Long, assignments: List<DayRoleAssignmentEntity>) {
        dayPlanDao.deleteRoleAssignments(dayPlanId)
        dayPlanDao.insertRoleAssignments(assignments.map { it.copy(id = 0, dayPlanId = dayPlanId) })
    }

    // 한 사람에게 브레이크 행이 둘 이상 생기면 읽는 쪽마다 다른 행을 집어 화면끼리 어긋난다 — 여기서 하나로 줄인다.
    suspend fun getBreaks(dayPlanId: Long): List<BreakAssignmentEntity> =
        dayPlanDao.getBreaks(dayPlanId).distinctBy { it.memberId }

    suspend fun saveBreaks(dayPlanId: Long, breaks: List<BreakAssignmentEntity>) {
        dayPlanDao.deleteBreaks(dayPlanId)
        dayPlanDao.insertBreaks(breaks.distinctBy { it.memberId }.map { it.copy(id = 0, dayPlanId = dayPlanId) })
    }

    // ── 배정 결과 저장/복원 (§3-5, §7-5 — 시드는 반드시 저장) ──
    suspend fun savePlan(dayPlanId: Long, freezeCursorMin: Int, output: RotationOutput): Long {
        // 읽는 쪽은 loadLatestPlan(최신 1건)뿐이라 예전 계획은 아무도 안 본다. 그런데 생성·셀 수정마다
        // 계획이 새로 쌓여서, 상시 거치 태블릿에서는 슬롯·셀까지 무한히 불어난다 — 저장 전에 정리한다.
        planDao.deletePlansForDayPlan(dayPlanId)   // slots·cells는 FK CASCADE로 같이 지워진다
        val planId = planDao.upsertPlan(
            RotationPlanEntity(dayPlanId = dayPlanId, seed = output.seed, score = output.score, freezeCursorMin = freezeCursorMin)
        )
        planDao.deleteSlots(planId)
        planDao.deleteCells(planId)
        planDao.insertSlots(output.slots.map { it.toEntity(planId) })
        planDao.insertCells(output.cells.map { it.toEntity(planId) })
        return planId
    }

    suspend fun loadLatestPlan(dayPlanId: Long): LoadedPlan? {
        val plan = planDao.getLatestForDayPlan(dayPlanId) ?: return null
        val slots = planDao.getSlots(plan.id).map { it.toDomain() }
        val cells = planDao.getCells(plan.id).map { it.toDomain() }
        return LoadedPlan(plan.id, plan.seed, slots, cells)
    }

    suspend fun updateCellPinned(planId: Long, slotIndex: Int, memberId: Long, isPinned: Boolean) =
        planDao.updatePinned(planId, slotIndex, memberId, isPinned)

    suspend fun updateCellPositionAndState(planId: Long, slotIndex: Int, memberId: Long, positionId: Long?, cellState: String) =
        planDao.updateCellPositionAndState(planId, slotIndex, memberId, positionId, cellState)

    suspend fun clearPlans(dayPlanId: Long) = planDao.deletePlansForDayPlan(dayPlanId)

    // 결과 그리드에서 이름 칸에 다른 멤버를 드래그해 놓았을 때 — 시간·포지션은 그대로 두고
    // 담당자만 맞바꾼다. 알고리즘을 다시 돌리지 않으므로 공정성 점수는 그대로 유지된다.
    suspend fun swapCellMembers(planId: Long, memberA: Long, memberB: Long) {
        val cells = planDao.getCells(planId)
        if (cells.none { it.memberId == memberA || it.memberId == memberB }) return
        val updated = cells.map { c ->
            when (c.memberId) {
                memberA -> c.copy(id = 0, memberId = memberB)
                memberB -> c.copy(id = 0, memberId = memberA)
                else -> c
            }
        }
        planDao.deleteCells(planId)
        planDao.insertCells(updated)
    }
}

// ═══════════════════ Entity ↔ core 모델 변환 ═══════════════════
// core/는 android 의존이 없어야 하므로 시각은 분(Int) 그대로 오간다. 변환은 enum·String뿐이다.

fun RotationPositionEntity.toDomain(): RotPosition = RotPosition(
    id = id,
    name = name,
    intensity = Intensity.valueOf(intensity),
    minCount = minCount,
    maxCount = maxCount,
    openPriority = openPriority,
    overflowPriority = overflowPriority,
    isActive = isActive
)

fun BreakAssignmentEntity.toDomain(): BreakSpan = BreakSpan(memberId, startMin, endMin)

fun Slot.toEntity(planId: Long): RotationSlotEntity = RotationSlotEntity(
    planId = planId,
    slotIndex = index,
    startMin = startMin,
    endMin = endMin,
    segmentType = type.name,
    isFrozen = isFrozen,
    remainderNote = remainderNote
)

fun RotationSlotEntity.toDomain(): Slot = Slot(
    index = slotIndex,
    startMin = startMin,
    endMin = endMin,
    type = SegmentType.valueOf(segmentType),
    isFrozen = isFrozen,
    remainderNote = remainderNote
)

fun Cell.toEntity(planId: Long): RotationCellEntity = RotationCellEntity(
    planId = planId,
    slotIndex = slotIndex,
    memberId = memberId,
    positionId = positionId,
    cellState = state.name,
    isPinned = isPinned,
    isManuallyEdited = isManuallyEdited
)

fun RotationCellEntity.toDomain(): Cell = Cell(
    slotIndex = slotIndex,
    memberId = memberId,
    positionId = positionId,
    state = CellState.valueOf(cellState),
    isPinned = isPinned,
    isManuallyEdited = isManuallyEdited
)

fun RotationSettingsEntity.toTimeConfig(): TimeConfig = TimeConfig(
    windowStart = windowStart,
    windowEnd = windowEnd,
    preBreakDesiredMinutes = preBreakDesiredMinutes,
    inBreakDesiredMinutes = inBreakDesiredMinutes,
    postBreakDesiredMinutes = postBreakDesiredMinutes,
    minSlotMinutes = minSlotMinutes,
    remainderPolicy = RemainderPolicy.valueOf(remainderPolicy),
    handoverMode = HandoverMode.valueOf(handoverMode),
    handoverMinutes = handoverMinutes,
    handoverMinSlotMinutes = handoverMinSlotMinutes
)

fun RotationSettingsEntity.toFairnessConfig(alphaOverride: Double? = null): FairnessConfig = FairnessConfig(
    alpha = alphaOverride ?: alpha,
    targetBasis = TargetBasis.valueOf(targetBasis),
    priority = FairnessPriority.valueOf(fairnessPriority),
    midBandMinutes = midBandMinutes
)

fun RotationSettingsEntity.toConstraintConfig(): ConstraintConfig = ConstraintConfig(
    // §8: "1 = 연속 금지"가 최솟값이다. 0 이하를 넣으면 첫 배정(run=1)부터 항상 위반으로 잡혀
    // 연속 여부와 무관하게 모든 칸이 위반 처리되므로 여기서 방어한다.
    samePositionMaxRun = samePositionMaxRun.coerceAtLeast(1),
    breakInterruptsRun = breakInterruptsRun,
    allowHighChain = allowHighChain,
    highMaxRun = highMaxRun,
    highCooldownSlots = highCooldownSlots,
    relaxPreBreak = relaxPreBreak
)

fun RotationSettingsEntity.toTierOrder(): List<Tier> =
    tierOrderCsv.split(",").filter { it.isNotBlank() }.map { Tier.valueOf(it) }
