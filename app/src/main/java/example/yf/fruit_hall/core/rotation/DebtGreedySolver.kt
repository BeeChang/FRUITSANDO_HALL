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

        for (slot in slots.filter { it.index !in frozenSlotIndices }) {
            val available = availableMembers(input, slot)
            val onBreak = breakingMembers(input, slot)

            onBreak.forEach { cells += Cell(slot.index, it, null, CellState.BREAK) }
            offMembers(input, slot, available, onBreak).forEach {
                cells += Cell(slot.index, it, null, CellState.OFF)
            }

            if (available.isEmpty()) continue

            val seats = PositionOpener.openSeats(input.positions, available.size)
            val seatPositions = orderSeatsByIntensity(seats, positionById, slot.index, shuffle)

            val remaining = available.toMutableList()
            for (positionId in seatPositions) {
                if (remaining.isEmpty()) break
                val position = positionId?.let { positionById[it] }
                val candidate = pickCandidate(remaining, debt, position, slot.index, shuffle)
                remaining.remove(candidate)
                cells += Cell(slot.index, candidate, positionId, CellState.ASSIGNED)
                if (position?.intensity == Intensity.HIGH) {
                    val alpha = if (slot.type == SegmentType.PRE_BREAK) input.fairness.alpha else 1.0
                    debt[candidate] = (debt[candidate] ?: 0.0) + slot.durationMin * alpha
                }
            }
        }
        return cells
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
