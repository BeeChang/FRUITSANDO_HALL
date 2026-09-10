package example.yf.fruit_hall.core.rotation

import kotlin.math.abs

// 명세 §4-5. 사용자가 브레이크 시각을 임의로 수정하면 앵커가 잘게 쪼개져 그리드가 부서진다.
// 해결책: 브레이크 시작/종료를 가장 가까운 슬롯 경계로 스냅한 뒤 그리드를 재생성한다.
// 2회 반복 후에도 안정되지 않으면 그대로 확정한다 (무한루프 방지).
object BreakSnapper {

    data class Result(val snappedBreaks: List<BreakSpan>, val grid: List<Slot>)

    fun snap(input: RotationInput): Result {
        var breaks = input.breaks
        var grid = TimeGridBuilder.build(input.copy(breaks = breaks))

        repeat(2) {
            val boundaries = grid.flatMap { listOf(it.startMin, it.endMin) }.distinct().sorted()
            if (boundaries.isEmpty()) return@repeat
            val snapped = breaks.map { b ->
                b.copy(
                    startMin = nearestBoundary(boundaries, b.startMin),
                    endMin = nearestBoundary(boundaries, b.endMin)
                )
            }
            if (snapped == breaks) return Result(breaks, grid)
            breaks = snapped
            grid = TimeGridBuilder.build(input.copy(breaks = breaks))
        }
        return Result(breaks, grid)
    }

    private fun nearestBoundary(boundaries: List<Int>, time: Int): Int =
        boundaries.minByOrNull { abs(it - time) } ?: time
}
