package example.yf.fruit_hall.core.rotation

// ═══════════════════ 도메인 모델 ═══════════════════
// 시각은 전부 자정 기준 분(0..1440) Int로 표현한다. core/는 android·java.time 의존이 없어야 하므로
// (CLAUDE.md 규약) DB·UI 계층에서만 "HH:mm" 문자열로 변환한다.

enum class Intensity { HIGH, LOW }
enum class SegmentType { PRE_BREAK, IN_BREAK, POST_BREAK }
enum class RemainderPolicy { REDISTRIBUTE, ABSORB, STANDALONE, DROP }
enum class FairnessPriority { HIGH, MID, LOW, OFF }
enum class TargetBasis { TOTAL_MINUTES, PRESENCE_RATIO }
enum class HandoverMode { DISPLAY_ONLY, DEDUCT }
enum class CellState { ASSIGNED, BREAK, OFF }
enum class Tier { CUMULATIVE_HIGH, CONSTRAINT, HIGH_VARIETY, LOW_FAIRNESS, TIEBREAK }

data class RotPosition(
    val id: Long,
    val name: String,
    val intensity: Intensity,
    val minCount: Int,
    val maxCount: Int?,          // null = 무제한
    val openPriority: Int,
    val overflowPriority: Int?,  // null = 잉여 안 받음
    val isActive: Boolean = true
)

data class Worker(
    val memberId: Long,
    val startMin: Int,
    val endMin: Int
)

data class BreakSpan(
    val memberId: Long,
    val startMin: Int,
    val endMin: Int
)

data class TimeConfig(
    val windowStart: Int,
    val windowEnd: Int,
    val preBreakDesiredMinutes: Int,
    val inBreakDesiredMinutes: Int?,   // null = 브레이크 평균 길이 사용
    val postBreakDesiredMinutes: Int,
    val minSlotMinutes: Int,
    val remainderPolicy: RemainderPolicy = RemainderPolicy.REDISTRIBUTE,
    val handoverMode: HandoverMode = HandoverMode.DISPLAY_ONLY,
    val handoverMinutes: Int = 5,
    val handoverMinSlotMinutes: Int = 20
)

data class FairnessConfig(
    val alpha: Double = 1.0,
    val targetBasis: TargetBasis = TargetBasis.TOTAL_MINUTES,
    val priority: FairnessPriority = FairnessPriority.HIGH,
    val midBandMinutes: Int = 15
)

data class ConstraintConfig(
    val samePositionMaxRun: Int = 1,
    val breakInterruptsRun: Boolean = true,
    val allowHighChain: Boolean = true,
    val highMaxRun: Int = 2,
    val highCooldownSlots: Int = 0,
    val relaxPreBreak: Boolean = true
)

data class SearchConfig(
    val seed: Long,
    val ilsIterations: Int = 6
)

/** 부분 재생성 시 확정 구간에서 넘어온 상태. §9 */
data class FrozenState(
    val freezeCursorMin: Int,
    val slots: List<Slot>,
    val cells: List<Cell>,
    val debtAtCursor: Map<Long, Double>
)

data class RotationInput(
    val workers: List<Worker>,
    val breaks: List<BreakSpan>,
    val positions: List<RotPosition>,
    val timeConfig: TimeConfig,
    val fairness: FairnessConfig,
    val constraints: ConstraintConfig,
    val tierOrder: List<Tier>,
    val search: SearchConfig,
    val frozen: FrozenState? = null,
    /**
     * 사용자가 핀으로 고정한 자리(§3-5 "재생성해도 유지"). 초기해를 만들 때 먼저 앉히고 시작한다 —
     * LocalSearch만 핀을 보면 그리디가 처음부터 다시 배정해 버려서 핀이 지켜지지 않는다.
     */
    val pinnedCells: List<PinnedAssignment> = emptyList()
)

/**
 * 핀은 슬롯 **번호**가 아니라 **시각**에 걸린다. 브레이크·근무시간을 바꾸면 앵커가 달라져 그리드가
 * 통째로 다시 짜이고(§4-1), 그때 슬롯 번호는 전혀 다른 시간대를 가리키게 된다 — 번호로 붙여두면
 * 핀이 엉뚱한 칸으로 옮겨간다.
 */
data class PinnedAssignment(
    val memberId: Long,
    val startMin: Int,
    val endMin: Int,
    val positionId: Long?,
    val isBreak: Boolean
)

data class Slot(
    val index: Int,
    val startMin: Int,
    val endMin: Int,
    val type: SegmentType,
    val isFrozen: Boolean = false,
    val remainderNote: String? = null
) {
    val durationMin: Int get() = endMin - startMin
}

data class Cell(
    val slotIndex: Int,
    val memberId: Long,
    val positionId: Long?,
    val state: CellState,
    val isPinned: Boolean = false,
    val isManuallyEdited: Boolean = false
)

data class Violation(
    val slotIndex: Int,
    val memberId: Long,
    val kind: String,
    val message: String
)

data class RotationOutput(
    val slots: List<Slot>,
    val cells: List<Cell>,
    val score: Long,
    val violations: List<Violation>,
    val debtCurve: Map<Long, List<Double>>,   // memberId -> 슬롯 경계별 누적 부채(분)
    val targetCurve: List<Double>,            // 슬롯 경계별 1인당 목표 누적 부채(분)
    val seed: Long
)
