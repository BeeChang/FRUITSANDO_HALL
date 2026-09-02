package example.yf.fruit_hall.ui.rotation

import example.yf.fruit_hall.core.rotation.CellState
import example.yf.fruit_hall.core.rotation.FairnessPriority
import example.yf.fruit_hall.core.rotation.HandoverMode
import example.yf.fruit_hall.core.rotation.RemainderPolicy
import example.yf.fruit_hall.core.rotation.SegmentType
import example.yf.fruit_hall.core.rotation.TargetBasis
import example.yf.fruit_hall.core.rotation.Tier
import example.yf.fruit_hall.core.rotation.Violation

data class MemberUi(val id: Long, val name: String, val colorHex: String)

data class RotationPositionUi(
    val id: Long,
    val name: String,
    val isHigh: Boolean,
    val minCount: Int,
    val maxCount: Int?,
    val openPriority: Int,
    val overflowPriority: Int?,
    val isActive: Boolean,
    val colorHex: String,
    val sortOrder: Int
)

data class RotationPresetUi(val id: Long, val name: String, val isDefault: Boolean, val alphaOverride: Double?)

data class RotationRoleUi(
    val id: Long,
    val presetId: Long,
    val label: String,
    val startMin: Int,
    val endMin: Int,
    val breakOrder: Int?,
    val breakMinutes: Int,
    val sortOrder: Int,
    val isActive: Boolean = true
)

data class RotationScheduleTemplateUi(
    val id: Long,
    val label: String,
    val startMin: Int,
    val endMin: Int,
    val breakOrder: Int?,
    val breakMinutes: Int,
    val sortOrder: Int
)

data class RoleAssignmentUi(
    val roleLabel: String,
    val memberId: Long?,
    val memberName: String?,
    val startMin: Int,
    val endMin: Int
)

data class BreakUi(val memberId: Long, val memberName: String, val startMin: Int, val endMin: Int, val isManual: Boolean)

data class RotationSettingsUi(
    val windowStart: Int = 11 * 60,
    val windowEnd: Int = 19 * 60,
    val preBreakDesiredMinutes: Int = 50,
    val inBreakDesiredMinutes: Int? = null,
    val postBreakDesiredMinutes: Int = 60,
    val minSlotMinutes: Int = 40,
    val defaultBreakStartMin: Int = 13 * 60 + 30,
    val defaultShiftDurationMinutes: Int = 9 * 60,
    val remainderPolicy: RemainderPolicy = RemainderPolicy.REDISTRIBUTE,
    val handoverMode: HandoverMode = HandoverMode.DISPLAY_ONLY,
    val handoverMinutes: Int = 5,
    val handoverMinSlotMinutes: Int = 20,
    val alpha: Double = 1.0,
    val targetBasis: TargetBasis = TargetBasis.TOTAL_MINUTES,
    val fairnessPriority: FairnessPriority = FairnessPriority.HIGH,
    val midBandMinutes: Int = 15,
    val samePositionMaxRun: Int = 1,
    val breakInterruptsRun: Boolean = true,
    val allowHighChain: Boolean = true,
    val highMaxRun: Int = 2,
    val highCooldownSlots: Int = 0,
    val relaxPreBreak: Boolean = true,
    val tierOrder: List<Tier> = listOf(Tier.CUMULATIVE_HIGH, Tier.CONSTRAINT, Tier.HIGH_VARIETY, Tier.LOW_FAIRNESS, Tier.TIEBREAK),
    val ilsIterations: Int = 6
)

data class RotationSlotUi(val index: Int, val startMin: Int, val endMin: Int, val type: SegmentType, val isFrozen: Boolean, val remainderNote: String?)

data class RotationCellUi(
    val slotIndex: Int,
    val state: CellState,
    val positionId: Long?,
    val positionName: String?,
    val colorHex: String?,
    val isPinned: Boolean,
    val hasViolation: Boolean
)

data class RotationRowUi(val memberId: Long, val name: String, val colorHex: String, val cells: List<RotationCellUi>)

data class SelectedCell(val slotIndex: Int, val memberId: Long)

data class RotationUiState(
    val date: String = "",
    val members: List<MemberUi> = emptyList(),
    val positions: List<RotationPositionUi> = emptyList(),
    val presets: List<RotationPresetUi> = emptyList(),
    val selectedPresetId: Long? = null,
    val roles: List<RotationRoleUi> = emptyList(),
    val scheduleTemplates: List<RotationScheduleTemplateUi> = emptyList(),
    val roleAssignments: List<RoleAssignmentUi> = emptyList(),
    val breaks: List<BreakUi> = emptyList(),
    val settings: RotationSettingsUi = RotationSettingsUi(),
    val seed: Long = 42L,
    val freezeCursorMin: Int? = null,

    val slots: List<RotationSlotUi> = emptyList(),
    val rows: List<RotationRowUi> = emptyList(),
    val score: Long? = null,
    val violations: List<Violation> = emptyList(),
    val debtCurve: Map<Long, List<Double>> = emptyMap(),
    val targetCurve: List<Double> = emptyList(),

    val isGenerating: Boolean = false,
    val selectedCell: SelectedCell? = null,

    val showMemberManage: Boolean = false,
    val showPositionManage: Boolean = false,
    val showPresetManage: Boolean = false,
    val showScheduleManage: Boolean = false,
    val showMemberAssign: Boolean = false,
    val showBreakEdit: Boolean = false,
    val showSettings: Boolean = false,

    val errorMessage: String? = null
)
