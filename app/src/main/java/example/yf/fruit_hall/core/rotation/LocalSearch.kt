package example.yf.fruit_hall.core.rotation

// 명세 §7-3·§7-4. 슬롯 내 swap/rotate만 사용 — 정원이 슬롯마다 달라 슬롯 간 이동은 절대 만들지 않는다.
// 엄격한 개선(<)만 수락 → 단조감소 + 유계 → 유한 종료 보장.
// 문제 규모(수십 슬롯, 수 명~십수 명)가 작아 매 후보마다 RotationScore를 전체 재계산해도
// 실사용에 문제없다 — 성능보다 정확성·단순성을 우선한다.
object LocalSearch {

    fun optimize(input: RotationInput, slots: List<Slot>, initialCells: List<Cell>, shuffle: SeededShuffle): List<Cell> {
        var (best, bestRank) = hillClimb(input, slots, initialCells, shuffle, iterationTag = -1)

        repeat(input.search.ilsIterations) { iter ->
            val perturbed = perturb(input, slots, best, shuffle, iter)
            val (climbed, rank) = hillClimb(input, slots, perturbed, shuffle, iterationTag = iter)
            if (RotationScore.compareRanking(rank, bestRank) < 0) {
                best = climbed
                bestRank = rank
            }
        }
        return best
    }

    private fun hillClimb(
        input: RotationInput,
        slots: List<Slot>,
        initial: List<Cell>,
        shuffle: SeededShuffle,
        iterationTag: Int
    ): Pair<List<Cell>, List<Long>> {
        var cells = initial
        // 가중합(score)이 아니라 계층별 raw의 사전식 비교로 우열을 가린다 — 가중합은 Long을 넘겨 뒤집힌다.
        var ranking = RotationScore.evaluate(input, slots, cells).ranking
        val movableSlots = slots.filter { !it.isFrozen }

        var improved = true
        while (improved) {
            improved = false
            val slotOrder = shuffle.shuffled(movableSlots, "neighbor_slot", iterationTag)
            for (slot in slotOrder) {
                val candidates = movableCandidateIndices(cells, slot.index)
                if (candidates.size < 2) continue
                val order = shuffle.shuffled(candidates.indices.toList(), "neighbor_pair", slot.index)

                for (oi in order) {
                    for (oj in order) {
                        if (oi >= oj) continue
                        val i = candidates[oi]
                        val j = candidates[oj]
                        val swapped = swapInSlot(cells, i, j)
                        val newRanking = RotationScore.evaluate(input, slots, swapped).ranking
                        if (RotationScore.compareRanking(newRanking, ranking) < 0) {
                            cells = swapped
                            ranking = newRanking
                            improved = true
                        }
                    }
                }

                if (candidates.size >= 3) {
                    val triples = shuffle.shuffled(candidates, "neighbor_triple", slot.index).take(candidates.size)
                    for (a in triples.indices) for (b in triples.indices) for (c in triples.indices) {
                        if (a == b || b == c || a == c) continue
                        val rotated = rotateInSlot(cells, candidates[a], candidates[b], candidates[c])
                        val newRanking = RotationScore.evaluate(input, slots, rotated).ranking
                        if (RotationScore.compareRanking(newRanking, ranking) < 0) {
                            cells = rotated
                            ranking = newRanking
                            improved = true
                        }
                    }
                }
            }
        }
        return cells to ranking
    }

    /** §7-3: 강제 섭동 — 개선 여부와 무관하게 임의 슬롯의 두 사람을 강제로 교환한 뒤 재등반한다. */
    private fun perturb(input: RotationInput, slots: List<Slot>, cells: List<Cell>, shuffle: SeededShuffle, iteration: Int): List<Cell> {
        val movableSlots = slots.filter { !it.isFrozen }
        val slotOrder = shuffle.shuffled(movableSlots, "perturb_slot", iteration)
        for (slot in slotOrder) {
            val candidates = movableCandidateIndices(cells, slot.index)
            if (candidates.size < 2) continue
            val pair = shuffle.shuffled(candidates, "perturb_pick", iteration).take(2)
            return swapInSlot(cells, pair[0], pair[1])
        }
        return cells
    }

    /** 핀 고정·확정 셀은 제외한, 이 슬롯에서 실제로 배정된(ASSIGNED) 셀의 인덱스 목록. */
    private fun movableCandidateIndices(cells: List<Cell>, slotIndex: Int): List<Int> =
        cells.indices.filter { idx ->
            val c = cells[idx]
            c.slotIndex == slotIndex && c.state == CellState.ASSIGNED && !c.isPinned
        }

    // ⚠ isManuallyEdited는 사용자가 §10-3으로 직접 고친 칸이라는 뜻이고, UI는 그 칸의 위반을
    // "의도된 예외"로 보고 숨긴다. 탐색이 만든 칸에 이 깃발을 달면 알고리즘이 만든 위반이
    // 화면에서 통째로 사라진다 — 여기서는 절대 건드리지 않고 원래 값을 그대로 둔다.

    /** 슬롯 내 두 사람의 포지션을 교환한다. 정원 제약을 절대 깨지 않는다 — 좌석 배열 원소 교환일 뿐이다. */
    private fun swapInSlot(cells: List<Cell>, indexA: Int, indexB: Int): List<Cell> {
        val a = cells[indexA]
        val b = cells[indexB]
        val result = cells.toMutableList()
        result[indexA] = a.copy(positionId = b.positionId)
        result[indexB] = b.copy(positionId = a.positionId)
        return result
    }

    /** 슬롯 내 세 사람의 포지션을 순환 교환한다 (a←b, b←c, c←a). */
    private fun rotateInSlot(cells: List<Cell>, indexA: Int, indexB: Int, indexC: Int): List<Cell> {
        val a = cells[indexA]
        val b = cells[indexB]
        val c = cells[indexC]
        val result = cells.toMutableList()
        result[indexA] = a.copy(positionId = b.positionId)
        result[indexB] = b.copy(positionId = c.positionId)
        result[indexC] = c.copy(positionId = a.positionId)
        return result
    }
}
