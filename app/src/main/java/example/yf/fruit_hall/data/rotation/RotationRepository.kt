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
    suspend fun setRoleActive(id: Long, isActive: Boolean) = presetDao.updateRoleActive(id, isActive)
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

    suspend fun saveRoleAssignments(dayPlanId: Long, assignments: List<DayRoleAssignmentEntity>) {
        dayPlanDao.deleteRoleAssignments(dayPlanId)
        dayPlanDao.insertRoleAssignments(assignments)
    }

    suspend fun getBreaks(dayPlanId: Long): List<BreakAssignmentEntity> = dayPlanDao.getBreaks(dayPlanId)

    suspend fun saveBreaks(dayPlanId: Long, breaks: List<BreakAssignmentEntity>) {
        dayPlanDao.deleteBreaks(dayPlanId)
        dayPlanDao.insertBreaks(breaks)
    }

    // ── 배정 결과 저장/복원 (§3-5, §7-5 — 시드는 반드시 저장) ──
    suspend fun savePlan(dayPlanId: Long, freezeCursorMin: Int, output: RotationOutput): Long {
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

    suspend fun updateCellPosition(planId: Long, slotIndex: Int, memberId: Long, positionId: Long?) =
        planDao.updateCellPosition(planId, slotIndex, memberId, positionId)

    suspend fun updateCellPinned(planId: Long, slotIndex: Int, memberId: Long, isPinned: Boolean) =
        planDao.updatePinned(planId, slotIndex, memberId, isPinned)
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
    samePositionMaxRun = samePositionMaxRun,
    breakInterruptsRun = breakInterruptsRun,
    allowHighChain = allowHighChain,
    highMaxRun = highMaxRun,
    highCooldownSlots = highCooldownSlots,
    relaxPreBreak = relaxPreBreak
)

fun RotationSettingsEntity.toTierOrder(): List<Tier> =
    tierOrderCsv.split(",").filter { it.isNotBlank() }.map { Tier.valueOf(it) }
