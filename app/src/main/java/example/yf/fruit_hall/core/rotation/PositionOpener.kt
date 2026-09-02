package example.yf.fruit_hall.core.rotation

// 명세 §5. 슬롯의 가용 인원 수만큼 "좌석 리스트"(positionId 순열)를 만든다.
// 반환값을 Map<positionId, count>가 아니라 List<Long?>(좌석 순열)로 두는 이유:
// 배정을 "좌석 배열과 사람 배열의 대응"으로 만들면 슬롯 내 swap이 배열 원소 교환이 되어
// 정원 제약을 구조적으로 절대 깰 수 없다 (§7-3). null = 유동 셀(포지션 미지정).
object PositionOpener {

    fun openSeats(positions: List<RotPosition>, availableCount: Int): List<Long?> {
        if (availableCount <= 0) return emptyList()
        val active = positions.filter { it.isActive }
        val seatCounts = linkedMapOf<Long, Int>()
        var remaining = availableCount

        // 1단계 — 필수 채우기: openPriority 오름차순, minCount만큼
        for (p in active.sortedBy { it.openPriority }) {
            if (remaining <= 0) break
            val take = minOf(p.minCount, remaining)
            if (take > 0) {
                seatCounts[p.id] = (seatCounts[p.id] ?: 0) + take
                remaining -= take
            }
        }

        // 2단계 — 잉여 분배: overflowPriority 오름차순, maxCount 여유만큼 라운드로빈
        val overflowCandidates = active.filter { it.overflowPriority != null }
            .sortedBy { it.overflowPriority }
        while (remaining > 0) {
            var distributed = false
            for (p in overflowCandidates) {
                if (remaining <= 0) break
                val cap = p.maxCount
                val current = seatCounts[p.id] ?: 0
                if (cap == null || current < cap) {
                    seatCounts[p.id] = current + 1
                    remaining -= 1
                    distributed = true
                }
            }
            if (!distributed) break
        }

        // 3단계 — overflowPriority 1위가 무제한이면 그곳에 몰아넣고, 아니면 유동 셀
        var floating = 0
        if (remaining > 0) {
            val top = overflowCandidates.firstOrNull()
            if (top != null && top.maxCount == null) {
                seatCounts[top.id] = (seatCounts[top.id] ?: 0) + remaining
                remaining = 0
            } else {
                floating = remaining
                remaining = 0
            }
        }

        val seats = mutableListOf<Long?>()
        seatCounts.forEach { (id, count) -> repeat(count) { seats += id } }
        repeat(floating) { seats += null }
        return seats
    }
}
