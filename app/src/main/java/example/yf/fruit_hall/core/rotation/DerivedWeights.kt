package example.yf.fruit_hall.core.rotation

// 명세 §6-1. 불변식: 하위 계층 이론 최대 총점 < 상위 계층 위반 1건.
// 손으로 가중치를 튜닝하지 않고, tierOrder(사용자가 드래그로 바꿀 수 있음)를 뒤에서부터
// (가장 낮은 우선순위부터) 순회하며 "지금까지 쌓은 하위 계층 이론 최대 총점 + 1"을
// 다음 계층의 단가로 삼는다. 계층 순서가 바뀌면 벽이 자동 재구축된다.
// ⚠ 이 도출식은 손으로 만지지 않는다 (명세 §14-7).
object DerivedWeights {

    fun derive(
        tierOrder: List<Tier>,
        slotCount: Int,
        workerCount: Int,
        highPositionCount: Int,
        totalWindowMinutes: Int
    ): Map<Tier, Long> {
        val s = slotCount.toLong().coerceAtLeast(1)
        val k = workerCount.toLong().coerceAtLeast(1)
        val t = totalWindowMinutes.toLong().coerceAtLeast(1)
        val numHigh = highPositionCount.toLong().coerceAtLeast(0)

        fun maxRaw(tier: Tier): Long = when (tier) {
            Tier.TIEBREAK -> 0L
            Tier.LOW_FAIRNESS -> k * t * t
            Tier.HIGH_VARIETY -> maxOf(numHigh, 1L) * k * t * t
            Tier.CONSTRAINT -> s * k * 4L
            Tier.CUMULATIVE_HIGH -> k * t * t * t
        }

        val weights = mutableMapOf<Tier, Long>()
        var cumulativeLowerMax = 0L
        for (tier in tierOrder.asReversed()) {
            val weight = if (weights.isEmpty()) 1L else (cumulativeLowerMax + 1L)
            weights[tier] = weight
            cumulativeLowerMax += weight * maxRaw(tier)
        }
        return weights
    }
}
