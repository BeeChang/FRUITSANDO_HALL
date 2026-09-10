package example.yf.fruit_hall.core.rotation

// 명세 §6. 5계층 벌점(낮을수록 좋음). 계층1이 이 기능의 핵심(누적 공평, §6-2).
object RotationScore {

    data class Result(
        /** 저장·표시용 요약값. 포화 연산이라 뒤집히진 않지만, **비교에 쓰면 안 된다** — [ranking]을 쓸 것. */
        val score: Long,
        /** tierOrder 순서의 계층별 raw. 해의 우열은 이 값의 사전식 비교로만 판단한다([compareRanking]). */
        val ranking: List<Long>,
        val violations: List<Violation>,
        val debtCurve: Map<Long, List<Double>>,
        val targetCurve: List<Double>
    )

    /**
     * 계층 사전식 비교 — 상위 계층이 다르면 하위 계층은 보지 않는다(§6-1 불변식).
     *
     * ⚠ 가중합(Σ weight×raw)으로 비교하면 안 된다. DerivedWeights의 계층1 가중치는 4.8e14 수준인데
     * 계층1 raw는 실사용에서 1e5~1e7이라 곱이 Long을 넘겨 값이 뒤집힌다. 그러면 점수 비교가 통째로
     * 무의미해져서 연속 위반도, 계층 우선순위 변경도 아무 효과가 없어진다.
     */
    fun compareRanking(a: List<Long>, b: List<Long>): Int {
        for (i in 0 until maxOf(a.size, b.size)) {
            val cmp = a.getOrElse(i) { 0L }.compareTo(b.getOrElse(i) { 0L })
            if (cmp != 0) return cmp
        }
        return 0
    }

    private fun saturatingMul(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        val result = a * b
        return if (result / b != a || result < 0L) Long.MAX_VALUE else result
    }

    private fun saturatingAdd(a: Long, b: Long): Long {
        val result = a + b
        return if (result < a) Long.MAX_VALUE else result
    }

    fun evaluate(input: RotationInput, slots: List<Slot>, cells: List<Cell>): Result {
        val positionById = input.positions.associateBy { it.id }
        val orderedSlots = slots.sortedBy { it.index }
        val members = input.workers.map { it.memberId }.distinct()
        val cellsBySlot = cells.groupBy { it.slotIndex }

        val weights = DerivedWeights.derive(
            tierOrder = input.tierOrder,
            slotCount = orderedSlots.size,
            workerCount = members.size,
            highPositionCount = input.positions.count { it.intensity == Intensity.HIGH },
            totalWindowMinutes = orderedSlots.sumOf { it.durationMin }
        )

        val cumulativeResult = evaluateCumulativeHigh(input, orderedSlots, cellsBySlot, positionById, members)
        val violations = evaluateConstraints(input, orderedSlots, cells)
        val varietyRaw = evaluateHighVariety(input, orderedSlots, cells, positionById, members)
        val fairnessRaw = evaluateLowFairness(input, orderedSlots, cells, positionById, members)

        val raw = mapOf(
            Tier.CUMULATIVE_HIGH to cumulativeResult.raw,
            Tier.CONSTRAINT to violations.size.toLong(),
            Tier.HIGH_VARIETY to varietyRaw,
            Tier.LOW_FAIRNESS to fairnessRaw,
            Tier.TIEBREAK to 0L
        )
        val score = input.tierOrder.fold(0L) { acc, tier ->
            saturatingAdd(acc, saturatingMul(weights[tier] ?: 0L, raw[tier] ?: 0L))
        }
        val ranking = input.tierOrder.map { raw[it] ?: 0L }

        return Result(score, ranking, violations, cumulativeResult.debtCurve, cumulativeResult.targetCurve)
    }

    private fun effectiveDuration(slot: Slot, tc: TimeConfig): Int {
        if (tc.handoverMode != HandoverMode.DEDUCT) return slot.durationMin
        if (slot.durationMin < tc.handoverMinSlotMinutes) return slot.durationMin
        return (slot.durationMin - tc.handoverMinutes).coerceAtLeast(1)
    }

    private class CumulativeResult(
        val raw: Long,
        val debtCurve: Map<Long, List<Double>>,
        val targetCurve: List<Double>
    )

    /** §6-2. 이 기능의 핵심 — 모든 슬롯 경계에서 누적 부채 편차를 측정해 합산한다. */
    private fun evaluateCumulativeHigh(
        input: RotationInput,
        slots: List<Slot>,
        cellsBySlot: Map<Int, List<Cell>>,
        positionById: Map<Long, RotPosition>,
        members: List<Long>
    ): CumulativeResult {
        val debt = mutableMapOf<Long, Double>().apply {
            members.forEach { put(it, input.frozen?.debtAtCursor?.get(it) ?: 0.0) }
        }
        val presence = mutableMapOf<Long, Double>().apply { members.forEach { put(it, 0.0) } }
        var totalHighMinutes = 0.0
        var totalPresenceMinutes = 0.0
        val debtCurve = members.associateWith { mutableListOf<Double>() }
        val targetCurve = mutableListOf<Double>()
        var raw = 0L
        val n = members.size.coerceAtLeast(1)

        for (slot in slots) {
            val dur = effectiveDuration(slot, input.timeConfig).toDouble()
            val alpha = if (slot.type == SegmentType.PRE_BREAK) input.fairness.alpha else 1.0
            val slotCells = cellsBySlot[slot.index].orEmpty()

            for (cell in slotCells) {
                if (cell.state != CellState.ASSIGNED) continue
                presence[cell.memberId] = (presence[cell.memberId] ?: 0.0) + dur
                totalPresenceMinutes += dur
                val position = cell.positionId?.let { positionById[it] } ?: continue
                if (position.intensity == Intensity.HIGH) {
                    val weighted = dur * alpha
                    debt[cell.memberId] = (debt[cell.memberId] ?: 0.0) + weighted
                    totalHighMinutes += weighted
                }
            }

            val isLastSlot = slot.index == slots.last().index
            var slotDeviationSum = 0.0
            var targetSum = 0.0
            for (p in members) {
                val target = when (input.fairness.targetBasis) {
                    TargetBasis.TOTAL_MINUTES -> totalHighMinutes / n
                    TargetBasis.PRESENCE_RATIO -> {
                        if (totalPresenceMinutes <= 0.0) 0.0
                        else totalHighMinutes * ((presence[p] ?: 0.0) / totalPresenceMinutes)
                    }
                }
                targetSum += target
                val rawDeviation = (debt[p] ?: 0.0) - target
                val deviation = adjustedDeviation(input.fairness, rawDeviation, isLastSlot)
                slotDeviationSum += deviation * deviation
                debtCurve.getValue(p).add(debt[p] ?: 0.0)
            }
            targetCurve.add(targetSum / n)
            raw += (dur.toLong() * slotDeviationSum.toLong())
        }
        return CumulativeResult(raw, debtCurve, targetCurve)
    }

    /** §6-4 누적 공평 우선도에 따라 유효 편차를 조정한다. */
    private fun adjustedDeviation(fairness: FairnessConfig, rawDeviation: Double, isLastSlot: Boolean): Double =
        when (fairness.priority) {
            FairnessPriority.HIGH -> rawDeviation
            FairnessPriority.MID -> if (kotlin.math.abs(rawDeviation) <= fairness.midBandMinutes) 0.0 else rawDeviation
            FairnessPriority.LOW -> if (isLastSlot) rawDeviation else 0.0
            FairnessPriority.OFF -> 0.0
        }

    private fun evaluateConstraints(input: RotationInput, slots: List<Slot>, cells: List<Cell>): List<Violation> {
        val c = input.constraints
        val slotById = slots.associateBy { it.index }
        val violations = mutableListOf<Violation>()
        val byMember = cells.groupBy { it.memberId }

        for ((memberId, memberCells) in byMember) {
            val timeline = memberCells.sortedBy { it.slotIndex }

            var sameRun = 0
            var lastPositionId: Long? = null
            var highRun = 0
            var cooldownRemaining = 0

            for (cell in timeline) {
                val slot = slotById[cell.slotIndex] ?: continue
                val relaxed = c.relaxPreBreak && slot.type == SegmentType.PRE_BREAK

                when (cell.state) {
                    CellState.ASSIGNED -> {
                        sameRun = if (cell.positionId != null && cell.positionId == lastPositionId) sameRun + 1 else 1
                        lastPositionId = cell.positionId
                        if (sameRun > c.samePositionMaxRun && !relaxed) {
                            violations += Violation(cell.slotIndex, memberId, "SAME_POSITION_RUN", "동일 포지션 연속 초과")
                        }

                        val isHigh = cell.positionId != null &&
                            input.positions.firstOrNull { it.id == cell.positionId }?.intensity == Intensity.HIGH

                        if (!c.allowHighChain) {
                            if (isHigh) {
                                if (cooldownRemaining > 0 && !relaxed) {
                                    violations += Violation(cell.slotIndex, memberId, "HIGH_COOLDOWN", "고강도 쿨다운 위반")
                                }
                                highRun += 1
                                if (highRun > c.highMaxRun && !relaxed) {
                                    violations += Violation(cell.slotIndex, memberId, "HIGH_CHAIN", "고강도 연속 초과")
                                }
                                cooldownRemaining = 0
                            } else {
                                if (highRun > 0) cooldownRemaining = c.highCooldownSlots
                                else if (cooldownRemaining > 0) cooldownRemaining -= 1
                                highRun = 0
                            }
                        }
                    }
                    CellState.BREAK -> {
                        if (c.breakInterruptsRun) {
                            sameRun = 0
                            lastPositionId = null
                        }
                    }
                    CellState.OFF -> {
                        sameRun = 0
                        lastPositionId = null
                        highRun = 0
                        cooldownRemaining = 0
                    }
                }
            }
        }
        return violations
    }

    /** §6-1 계층3. 고강도 종류가 1개 이하면 계층1과 완전히 동일해져 이중 벌점이 걸리므로 0으로 무력화. */
    private fun evaluateHighVariety(
        input: RotationInput,
        slots: List<Slot>,
        cells: List<Cell>,
        positionById: Map<Long, RotPosition>,
        members: List<Long>
    ): Long {
        val highPositions = input.positions.filter { it.intensity == Intensity.HIGH }
        if (highPositions.size <= 1) return 0L
        return positionVarianceRaw(slots, cells, positionById, members, highPositions.map { it.id }.toSet())
    }

    private fun evaluateLowFairness(
        input: RotationInput,
        slots: List<Slot>,
        cells: List<Cell>,
        positionById: Map<Long, RotPosition>,
        members: List<Long>
    ): Long {
        val lowPositions = input.positions.filter { it.intensity == Intensity.LOW }
        if (lowPositions.isEmpty()) return 0L
        val minutesRaw = positionVarianceRaw(slots, cells, positionById, members, lowPositions.map { it.id }.toSet())

        // 경험 포지션 종류 수 편차 (§6-1: "경험 포지션 종류 수")
        val distinctByMember = members.associateWith { mutableSetOf<Long>() }
        for (cell in cells) {
            if (cell.state != CellState.ASSIGNED) continue
            val position = cell.positionId?.let { positionById[it] } ?: continue
            if (position.intensity == Intensity.LOW) distinctByMember[cell.memberId]?.add(position.id)
        }
        val counts = members.map { (distinctByMember[it]?.size ?: 0).toDouble() }
        val avg = if (counts.isEmpty()) 0.0 else counts.average()
        val varietyRaw = counts.sumOf { val d = it - avg; d * d }.toLong()

        return minutesRaw + varietyRaw
    }

    private fun positionVarianceRaw(
        slots: List<Slot>,
        cells: List<Cell>,
        positionById: Map<Long, RotPosition>,
        members: List<Long>,
        targetPositionIds: Set<Long>
    ): Long {
        val slotDuration = slots.associateBy({ it.index }, { it.durationMin })
        val minutesByPositionAndMember = mutableMapOf<Long, MutableMap<Long, Double>>()
        for (cell in cells) {
            if (cell.state != CellState.ASSIGNED) continue
            val positionId = cell.positionId ?: continue
            if (positionId !in targetPositionIds) continue
            val dur = slotDuration[cell.slotIndex]?.toDouble() ?: continue
            minutesByPositionAndMember
                .getOrPut(positionId) { mutableMapOf() }
                .merge(cell.memberId, dur, Double::plus)
        }
        var raw = 0.0
        val k = members.size.coerceAtLeast(1)
        for (positionId in targetPositionIds) {
            val perMember = minutesByPositionAndMember[positionId] ?: emptyMap()
            val total = perMember.values.sum()
            val avg = total / k
            for (memberId in members) {
                val d = (perMember[memberId] ?: 0.0) - avg
                raw += d * d
            }
        }
        return raw.toLong()
    }
}
