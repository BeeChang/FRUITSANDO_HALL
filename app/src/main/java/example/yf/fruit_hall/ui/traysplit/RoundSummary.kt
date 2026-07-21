package example.yf.fruit_hall.ui.traysplit

/** 차수 하나에 배정된 판 한 장의 내용물 */
data class RoundTraySummary(
    val trayId: Long,
    val items: List<TrayItemUi>
)

/** 차수 하나에 대한 요약: 배정된 판 수, 대략적 개수(개별 개수 미상 시 50개 기준), 들어있는 품목(중복 제거), 판별 내용 */
data class RoundSummary(
    val round: Int,
    val trayCount: Int,
    val approxQty: Int,
    val items: List<TrayItemUi>,
    val trays: List<RoundTraySummary>
)

/** 1차에 배정됐지만 판의 진열 위치(spaceId)가 primaryLocation과 다른 판 id 집합 — "이동" 배지 대상 */
fun needsMoveTrayIds(trays: List<TrayUi>, assignment: Map<Long, Int>, primaryLocationSpaceId: Long?): Set<Long> {
    if (primaryLocationSpaceId == null) return emptySet()
    return trays.filter { assignment[it.id] == 1 && it.spaceId != primaryLocationSpaceId }
        .mapTo(mutableSetOf()) { it.id }
}

/** 오늘 사용 중인(활성) 품목 중 1차에 하나도 배정되지 않은 것 — 등록 누락 확인용 */
fun missingActiveTypes(snackTypes: List<SnackTypeUi>, trays: List<TrayUi>, assignment: Map<Long, Int>): List<SnackTypeUi> {
    val round1TypeIds = trays.filter { assignment[it.id] == 1 }
        .flatMap { it.items }.mapTo(mutableSetOf()) { it.snackTypeId }
    return snackTypes.filter { it.isActive && it.id !in round1TypeIds }
}

fun buildRoundSummaries(trays: List<TrayUi>, assignment: Map<Long, Int>, rounds: Int): List<RoundSummary> {
    return (1..rounds).map { round ->
        val roundTrays = trays.filter { assignment[it.id] == round }
        val allItems = roundTrays.flatMap { it.items }
        val distinctItems = allItems.distinctBy { it.snackTypeId }.sortedBy { it.sortOrder }
        val approxQty = allItems.sumOf { it.exactQty ?: it.roughSize?.nominal ?: 50 }
        RoundSummary(
            round = round,
            trayCount = roundTrays.size,
            approxQty = approxQty,
            items = distinctItems,
            trays = roundTrays.map { tray -> RoundTraySummary(tray.id, tray.items.sortedBy { it.sortOrder }) }
        )
    }
}
