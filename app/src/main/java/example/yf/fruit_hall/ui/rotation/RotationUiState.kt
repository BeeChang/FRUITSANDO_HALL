package example.yf.fruit_hall.ui.rotation

import androidx.annotation.StringRes
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

data class RotationScheduleTemplateUi(
    val id: Long,
    val label: String,
    val startMin: Int,
    val endMin: Int,
    val breakStartMin: Int?,
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

/**
 * 표의 한 행 = 사람 한 명 (§10-2 "세로축 = 사람 / 가로축 = 시간").
 * 아직 사람이 안 꽂힌 근무 슬롯도 빈 행으로 보여줘서 그 자리에 드래그로 배정할 수 있게 한다.
 */
data class RotationRowUi(
    val roleLabel: String,
    val memberId: Long?,
    val name: String,
    val colorHex: String,
    val startMin: Int,
    val endMin: Int,
    /** 오늘 이 줄에 적용한 근무 스케줄 이름. 없으면(아직 안 넣었으면) 이름 칸에 시간 범위를 대신 보여준다. */
    val scheduleLabel: String?,
    /** 표에는 보이지만 포지션 배정에서는 빠진 사람. */
    val isExcluded: Boolean,
    val cells: List<RotationCellUi>
)

/** 스케줄 탭에 저장된 오늘 근무자 한 명. 근무조(포지션2 배정)와는 별개로, 원본은 스케줄 탭이다. */
data class TodayShiftUi(val personName: String, val shift: String)

data class SelectedCell(val slotIndex: Int, val memberId: Long)

/**
 * 배정 초기화 범위. 표는 근무스케줄(시간) → 사람 → 포지션(생성 결과) 순으로 얹혀 있어서,
 * 아래 것을 지우면 위에 얹힌 것도 같이 지워져야 앞뒤가 맞는다. 그래서 어떤 값을 골라도
 * 포지션(생성 결과)은 항상 지워진다 — 사람이나 시간이 바뀌면 예전 결과표는 이미 틀린 표다.
 */
enum class RotationResetTarget {
    /** 생성 결과만 지운다. 사람·근무스케줄은 그대로. */
    POSITIONS,
    /** 사람을 미배정으로 되돌린다. 근무스케줄은 그대로. */
    MEMBERS,
    /** 근무스케줄(시각·스케줄 이름·브레이크)을 되돌린다. 사람은 그대로. */
    SCHEDULES,
    /** 전부 되돌린다. */
    ALL
}

data class RotationUiState(
    val date: String = "",
    val members: List<MemberUi> = emptyList(),
    val positions: List<RotationPositionUi> = emptyList(),
    val presets: List<RotationPresetUi> = emptyList(),
    val selectedPresetId: Long? = null,
    val scheduleTemplates: List<RotationScheduleTemplateUi> = emptyList(),
    /** 스케줄 탭의 오늘 날짜 근무자. 비어 있으면 오늘 데이터가 없다는 뜻이라 상단 칩을 띄우지 않는다. */
    val todayShifts: List<TodayShiftUi> = emptyList(),
    val roleAssignments: List<RoleAssignmentUi> = emptyList(),
    val breaks: List<BreakUi> = emptyList(),
    val settings: RotationSettingsUi = RotationSettingsUi(),
    val seed: Long = 42L,

    // 표는 항상 하나다. 생성 전에는 근무시간만 칠해진 초안, 생성 후에는 포지션이 채워진 결과.
    val slots: List<RotationSlotUi> = emptyList(),
    val rows: List<RotationRowUi> = emptyList(),
    val isDraft: Boolean = true,
    val score: Long? = null,
    val violations: List<Violation> = emptyList(),
    val debtCurve: Map<Long, List<Double>> = emptyMap(),
    val targetCurve: List<Double> = emptyList(),

    val isGenerating: Boolean = false,
    val selectedCell: SelectedCell? = null,

    val showMemberManage: Boolean = false,
    val showPresetManage: Boolean = false,
    val showBreakEdit: Boolean = false,
    val showSettings: Boolean = false,
    val showCursorPicker: Boolean = false,

    val errorMessage: RotationMessage? = null
)

/**
 * 하단 바에 잠깐 뜨는 안내 문구. 대부분은 strings.xml에 있는 고정 문구라 [Res]로 넘기고,
 * 알고리즘이 던진 예외 메시지처럼 미리 적어둘 수 없는 것만 [Raw]로 넘긴다 —
 * ViewModel은 Context를 들지 않으므로 문자열로 푸는 건 화면 쪽에서 한다.
 */
sealed interface RotationMessage {
    data class Res(@StringRes val id: Int) : RotationMessage
    data class Raw(val text: String) : RotationMessage
}
