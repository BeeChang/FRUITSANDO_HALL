package example.yf.fruit_hall.core.rotation

// 명세 §7-2. 초기해 — 부채 기반 그리디. 최적화 없이도 편차가 슬롯 길이 이내로 들어오는 실사용 품질.
// ⚠ 동점 처리·포지션 순회 순서에 시드 셔플을 반드시 적용한다 (§7-4). 안 하면 매일 같은 표가 나온다.
object DebtGreedySolver {

    fun solve(input: RotationInput, slots: List<Slot>, shuffle: SeededShuffle): List<Cell> {
        val positionById = input.positions.associateBy { it.id }
        val cells = mutableListOf<Cell>()

        val frozenCells = input.frozen?.cells.orEmpty()
        cells += frozenCells
        val frozenSlotIndices = frozenCells.map { it.slotIndex }.toSet()

        val debt = mutableMapOf<Long, Double>().apply {
            input.workers.forEach { put(it.memberId, input.frozen?.debtAtCursor?.get(it.memberId) ?: 0.0) }
        }

        // §7-2 "후보 = … 제약 위반 안 하는 사람". 연속 상태를 슬롯마다 이어가며 추적한다.
        // 확정 구간이 있으면 그 끝 상태에서 이어받아야 경계에서 연속이 새로 생기지 않는다.
        val runState = RunState(input.constraints)
        frozenCells.sortedBy { it.slotIndex }.forEach { runState.advance(it.memberId, it.state, it.positionId) }

        // §3-5: 핀은 재생성해도 유지된다. 슬롯 번호가 아니라 시각으로 맞춰야 그리드가 다시 짜여도
        // 같은 시간대에 붙는다 — 슬롯 한가운데를 덮는 핀을 그 슬롯의 핀으로 본다.
        fun pinsFor(slot: Slot): List<PinnedAssignment> {
            val mid = (slot.startMin + slot.endMin) / 2
            return input.pinnedCells.filter { it.startMin <= mid && mid < it.endMin }
        }

        for (slot in slots.filter { it.index !in frozenSlotIndices }) {
            val slotPins = pinsFor(slot)
            // 핀 걸린 휴게는 그 시간에 자리를 비우겠다는 뜻이다 — 가용 인원에서 빼야 정원 계산이 맞는다.
            val pinnedBreakMembers = slotPins.filter { it.isBreak }.map { it.memberId }.toSet()
            val rawAvailable = availableMembers(input, slot)
            val available = rawAvailable.filterNot { it in pinnedBreakMembers }
            val onBreak = breakingMembers(input, slot) + rawAvailable.filter { it in pinnedBreakMembers }

            onBreak.forEach {
                cells += Cell(slot.index, it, null, CellState.BREAK, isPinned = it in pinnedBreakMembers)
                runState.advance(it, CellState.BREAK, null)
            }
            offMembers(input, slot, available, onBreak).forEach {
                cells += Cell(slot.index, it, null, CellState.OFF)
                runState.advance(it, CellState.OFF, null)
            }

            if (available.isEmpty()) continue

            val seats = PositionOpener.openSeats(input.positions, available.size)
            val seatPositions = orderSeatsByIntensity(seats, positionById, slot.index, shuffle)

            // 이 구간이 제약 완화 대상이면 굳이 후보를 좁히지 않는다 — 점수에도 안 잡히는 제약을
            // 그리디만 지키면 부채 균형(계층1)만 애꿎게 나빠진다.
            val relaxed = input.constraints.relaxPreBreak && slot.type == SegmentType.PRE_BREAK

            val remaining = available.toMutableList()
            val seatQueue = seatPositions.toMutableList()

            // 핀 먼저 — 그 사람과 그 자리를 빼놓고 나머지를 배정한다. 이 슬롯에 그 자리가 열리지 않았거나
            // 그 사람이 자리를 비웠으면 무시한다(정원을 깨면서까지 지키지는 않는다).
            for (pin in slotPins.filter { !it.isBreak && it.positionId != null }.distinctBy { it.memberId }) {
                if (pin.memberId !in remaining) continue
                val seatIdx = seatQueue.indexOf(pin.positionId)
                if (seatIdx < 0) continue
                seatQueue.removeAt(seatIdx)
                remaining.remove(pin.memberId)
                cells += Cell(slot.index, pin.memberId, pin.positionId, CellState.ASSIGNED, isPinned = true)
                runState.advance(pin.memberId, CellState.ASSIGNED, pin.positionId)
                if (positionById[pin.positionId]?.intensity == Intensity.HIGH) {
                    val alpha = if (slot.type == SegmentType.PRE_BREAK) input.fairness.alpha else 1.0
                    debt[pin.memberId] = (debt[pin.memberId] ?: 0.0) + slot.durationMin * alpha
                }
            }

            for (positionId in seatQueue) {
                if (remaining.isEmpty()) break
                val position = positionId?.let { positionById[it] }
                // 위반을 안 만드는 사람만 우선 후보로 본다. 전부 위반이면(회피 불가) 소프트 제약이므로
                // 그냥 전원을 후보로 되돌린다 — 해가 없다고 죽으면 안 된다(§8).
                val eligible = if (relaxed || positionId == null) remaining
                else remaining.filter { runState.canTake(it, positionId) }.ifEmpty { remaining }
                val candidate = pickCandidate(eligible, debt, position, slot.index, shuffle)
                remaining.remove(candidate)
                cells += Cell(slot.index, candidate, positionId, CellState.ASSIGNED)
                runState.advance(candidate, CellState.ASSIGNED, positionId)
                if (position?.intensity == Intensity.HIGH) {
                    val alpha = if (slot.type == SegmentType.PRE_BREAK) input.fairness.alpha else 1.0
                    debt[candidate] = (debt[candidate] ?: 0.0) + slot.durationMin * alpha
                }
            }
        }
        return cells
    }

    /**
     * 사람별 "같은 포지션 연속" 상태. 전이 규칙은 RotationScore.evaluateConstraints와 반드시 같아야 한다 —
     * 다르면 그리디가 피한 것을 점수는 위반으로 잡거나 그 반대가 된다.
     */
    private class RunState(private val constraints: ConstraintConfig) {
        private val lastPosition = mutableMapOf<Long, Long?>()
        private val sameRun = mutableMapOf<Long, Int>()

        /** memberId를 positionId에 넣어도 '동일 포지션 연속 허용 슬롯 수'를 넘지 않는가. */
        fun canTake(memberId: Long, positionId: Long): Boolean {
            if (lastPosition[memberId] != positionId) return true
            return (sameRun[memberId] ?: 0) + 1 <= constraints.samePositionMaxRun
        }

        fun advance(memberId: Long, state: CellState, positionId: Long?) {
            when (state) {
                CellState.ASSIGNED -> {
                    sameRun[memberId] =
                        if (positionId != null && positionId == lastPosition[memberId]) (sameRun[memberId] ?: 0) + 1 else 1
                    lastPosition[memberId] = positionId
                }
                CellState.BREAK -> if (constraints.breakInterruptsRun) {
                    sameRun[memberId] = 0
                    lastPosition[memberId] = null
                }
                CellState.OFF -> {
                    sameRun[memberId] = 0
                    lastPosition[memberId] = null
                }
            }
        }
    }

    private fun availableMembers(input: RotationInput, slot: Slot): List<Long> =
        input.workers.filter { it.startMin <= slot.startMin && it.endMin >= slot.endMin }
            .map { it.memberId }
            .filter { memberId -> input.breaks.none { it.memberId == memberId && overlaps(it, slot) } }

    private fun breakingMembers(input: RotationInput, slot: Slot): List<Long> =
        input.workers.filter { it.startMin <= slot.startMin && it.endMin >= slot.endMin }
            .map { it.memberId }
            .filter { memberId -> input.breaks.any { it.memberId == memberId && overlaps(it, slot) } }

    private fun offMembers(input: RotationInput, slot: Slot, available: List<Long>, onBreak: List<Long>): List<Long> =
        input.workers.map { it.memberId }.distinct() - available.toSet() - onBreak.toSet()

    private fun overlaps(b: BreakSpan, slot: Slot): Boolean = b.startMin < slot.endMin && b.endMin > slot.startMin

    /** §7-4: 같은 강도끼리 포지션 순회 순서도 반드시 셔플한다. HIGH를 먼저 채운다. */
    private fun orderSeatsByIntensity(
        seats: List<Long?>,
        positionById: Map<Long, RotPosition>,
        slotIndex: Int,
        shuffle: SeededShuffle
    ): List<Long?> {
        val high = seats.filter { it == null || positionById[it]?.intensity == Intensity.HIGH }
        val low = seats.filter { it != null && positionById[it]?.intensity == Intensity.LOW }
        val highShuffled = shuffle.shuffled(high, "position_high", slotIndex)
        val lowShuffled = shuffle.shuffled(low, "position_low", slotIndex)
        return highShuffled + lowShuffled
    }

    private fun pickCandidate(
        remaining: List<Long>,
        debt: Map<Long, Double>,
        position: RotPosition?,
        slotIndex: Int,
        shuffle: SeededShuffle
    ): Long {
        if (position == null) {
            return shuffle.shuffled(remaining, "member_floating", slotIndex).first()
        }
        // §7-2: HIGH는 부채 오름차순(가장 안 힘든 사람 먼저), LOW는 내림차순(부채 많은 사람부터 쉬는 자리로)
        val sorted = if (position.intensity == Intensity.HIGH) {
            remaining.sortedBy { debt[it] ?: 0.0 }
        } else {
            remaining.sortedByDescending { debt[it] ?: 0.0 }
        }
        val extreme = debt[sorted.first()] ?: 0.0
        val tied = sorted.filter { (debt[it] ?: 0.0) == extreme }
        return shuffle.shuffled(tied, "member", slotIndex).first()
    }
}
