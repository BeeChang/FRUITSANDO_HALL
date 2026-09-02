package example.yf.fruit_hall.core.rotation

import kotlin.math.roundToInt

// 앵커 → 구간 → 슬롯 (명세 §4). 순수 함수, 상태 없음.
object TimeGridBuilder {

    fun build(input: RotationInput): List<Slot> {
        val tc = input.timeConfig
        val breakBoundaries = breakBoundaryTimes(input.breaks)
        val anchors = collectAnchors(input)
        val rawSegments = anchors.zipWithNext { s, e -> s to e }.filter { it.second > it.first }

        val naive = rawSegments.map { (start, end) ->
            val type = classify(start, input.breaks)
            val desired = desiredLength(type, tc, input.breaks)
            splitSegment(start, end, desired, tc.minSlotMinutes, type)
        }

        val resolved = applyRemainderPolicy(naive, breakBoundaries, tc.remainderPolicy, tc.minSlotMinutes)

        return resolved.flatten().mapIndexed { index, slot -> slot.copy(index = index) }
    }

    private fun collectAnchors(input: RotationInput): List<Int> {
        val tc = input.timeConfig
        val set = sortedSetOf(tc.windowStart, tc.windowEnd)
        input.workers.forEach { w ->
            if (w.startMin in tc.windowStart..tc.windowEnd) set += w.startMin
            if (w.endMin in tc.windowStart..tc.windowEnd) set += w.endMin
        }
        input.breaks.forEach { b ->
            if (b.startMin in tc.windowStart..tc.windowEnd) set += b.startMin
            if (b.endMin in tc.windowStart..tc.windowEnd) set += b.endMin
        }
        return set.toList()
    }

    private fun breakBoundaryTimes(breaks: List<BreakSpan>): Set<Int> {
        val set = mutableSetOf<Int>()
        breaks.forEach { set += it.startMin; set += it.endMin }
        return set
    }

    private fun classify(segmentStart: Int, breaks: List<BreakSpan>): SegmentType {
        if (breaks.isEmpty()) return SegmentType.PRE_BREAK
        val firstBreakStart = breaks.minOf { it.startMin }
        val lastBreakEnd = breaks.maxOf { it.endMin }
        val anyOngoing = breaks.any { segmentStart >= it.startMin && segmentStart < it.endMin }
        return when {
            anyOngoing -> SegmentType.IN_BREAK
            segmentStart < firstBreakStart -> SegmentType.PRE_BREAK
            segmentStart >= lastBreakEnd -> SegmentType.POST_BREAK
            else -> SegmentType.IN_BREAK
        }
    }

    private fun desiredLength(type: SegmentType, tc: TimeConfig, breaks: List<BreakSpan>): Int {
        return when (type) {
            SegmentType.PRE_BREAK -> tc.preBreakDesiredMinutes
            SegmentType.POST_BREAK -> tc.postBreakDesiredMinutes
            SegmentType.IN_BREAK -> tc.inBreakDesiredMinutes
                ?: breaks.map { it.endMin - it.startMin }.average().roundToInt().coerceAtLeast(1)
        }
    }

    /** §4-3: n = round(len/desired) (최소 1); len/n < minSlot && n>1 이면 n-=1 재계산. */
    private fun splitSegment(start: Int, end: Int, desired: Int, minSlot: Int, type: SegmentType): List<Slot> {
        val len = end - start
        var n = (len.toDouble() / desired).roundToInt().coerceAtLeast(1)
        if (n > 1 && len.toDouble() / n < minSlot) n -= 1

        val boundaries = (0..n).map { i -> start + ((len.toLong() * i) / n).toInt() }
            .toMutableList().also { it[it.lastIndex] = end }

        return (0 until n).map { i ->
            Slot(index = 0, startMin = boundaries[i], endMin = boundaries[i + 1], type = type)
        }
    }

    /**
     * §4-4 자투리 처리. n==1인데도 슬롯 길이가 minSlot 미만인 "진짜 자투리" 구간만 대상.
     * ABSORB·REDISTRIBUTE 둘 다 이전 구간과의 경계가 브레이크 앵커면 불가능 — 그 경계는
     * 사람이 실제로 자리를 비우는 시각이라 옮길 수 없기 때문이다 (§4-4 경고). 이 경우 STANDALONE 폴백.
     */
    private fun applyRemainderPolicy(
        segments: List<List<Slot>>,
        breakBoundaries: Set<Int>,
        policy: RemainderPolicy,
        minSlot: Int
    ): List<List<Slot>> {
        val result = segments.toMutableList()
        for (i in result.indices) {
            val seg = result[i]
            if (seg.size != 1 || seg[0].durationMin >= minSlot) continue
            val remainderSlot = seg[0]
            val boundaryIsBreak = breakBoundaries.contains(remainderSlot.startMin)

            if (policy == RemainderPolicy.DROP) {
                result[i] = emptyList()
                continue
            }
            var prevIdx = i - 1
            while (prevIdx >= 0 && result[prevIdx].isEmpty()) prevIdx--

            if (i == 0 || boundaryIsBreak || prevIdx < 0) {
                result[i] = listOf(
                    remainderSlot.copy(
                        remainderNote = if (boundaryIsBreak)
                            "브레이크 경계로 흡수 불가 → 독립 슬롯" else "독립 슬롯"
                    )
                )
                continue
            }
            val prevSeg = result[prevIdx]
            when (policy) {
                RemainderPolicy.ABSORB -> {
                    val lastPrevSlot = prevSeg.last()
                    result[prevIdx] = prevSeg.dropLast(1) + lastPrevSlot.copy(endMin = remainderSlot.endMin)
                    result[i] = emptyList()
                }
                RemainderPolicy.REDISTRIBUTE -> {
                    val combinedStart = prevSeg.first().startMin
                    val combinedEnd = remainderSlot.endMin
                    val totalSlots = prevSeg.size + 1
                    val len = combinedEnd - combinedStart
                    val boundaries = (0..totalSlots).map { k ->
                        combinedStart + ((len.toLong() * k) / totalSlots).toInt()
                    }.toMutableList().also { it[it.lastIndex] = combinedEnd }
                    val type = prevSeg.first().type
                    result[prevIdx] = (0 until totalSlots).map { k ->
                        Slot(index = 0, startMin = boundaries[k], endMin = boundaries[k + 1], type = type)
                    }
                    result[i] = emptyList()
                }
                else -> Unit // STANDALONE, DROP 은 위에서 처리됨
            }
        }
        return result
    }
}
