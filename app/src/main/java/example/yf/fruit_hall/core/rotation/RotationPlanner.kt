package example.yf.fruit_hall.core.rotation

// 명세 §7-1 파이프라인 오케스트레이션. TrayAllocator가 하던 "엮기" 역할.
// 그리드 생성 → 브레이크 스냅 → 초기해(부채 그리디) → 힐클라이밍+ILS → 점수 평가.
class RotationPlanner {

    fun plan(input: RotationInput): RotationOutput {
        val snapResult = BreakSnapper.snap(input)
        val effectiveInput = input.copy(breaks = snapResult.snappedBreaks)

        val frozenSlotIndices = input.frozen?.cells?.map { it.slotIndex }?.toSet().orEmpty()
        val slots = snapResult.grid.map { slot ->
            if (slot.index in frozenSlotIndices) slot.copy(isFrozen = true) else slot
        }

        val shuffle = SeededShuffle(effectiveInput.search.seed)
        val initialCells = DebtGreedySolver.solve(effectiveInput, slots, shuffle)
        val optimizedCells = LocalSearch.optimize(effectiveInput, slots, initialCells, shuffle)
        val result = RotationScore.evaluate(effectiveInput, slots, optimizedCells)

        return RotationOutput(
            slots = slots,
            cells = optimizedCells,
            score = result.score,
            violations = result.violations,
            debtCurve = result.debtCurve,
            targetCurve = result.targetCurve,
            seed = effectiveInput.search.seed
        )
    }
}
