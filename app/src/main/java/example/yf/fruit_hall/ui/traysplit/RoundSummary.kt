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

/** 차수별 실제 배정 품목을 콤마로 나열한 기본 문구 — 다이얼로그에서 차수별 품목 입력칸의 초기값으로 사용 */
fun defaultRoundItemsText(summary: RoundSummary): String =
    summary.items.joinToString(", ") { it.name }

/** 차수별 총정리를 디스코드 전송용 텍스트로 조합: "7/23일(목) 산도 라인업 \n\n 1차(11시) : 품목, 품목 \n\n 2차(14시30분) : ..." */
fun buildDiscordSummaryMessage(
    dateText: String,
    summaries: List<RoundSummary>,
    roundTimes: Map<Int, String>,
    roundItemsText: Map<Int, String>,
    extraText: String
): String {
    val body = summaries.joinToString("\n\n") { summary ->
        val time = roundTimes[summary.round].orEmpty().trim()
        val header = if (time.isEmpty()) "${summary.round}차" else "${summary.round}차(${time})"
        val items = roundItemsText[summary.round].orEmpty().trim().ifEmpty { "-" }
        "$header : $items"
    }
    return listOf(dateText.trim(), body, extraText.trim())
        .filter { it.isNotEmpty() }
        .joinToString("\n\n")
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
