package example.yf.fruit_hall.ui.traysplit.component

/**
 * core.TrayAllocator의 strategyName(엔진 내부 식별용 한글 라벨)을
 * 비개발자도 이해하기 쉬운 화면 문구로 바꿔준다. 엔진 로직/문자열 자체는 건드리지 않는다.
 */
fun strategyDisplayName(strategyName: String): String = when (strategyName) {
    "현장 관행형" -> "섞인 판 먼저"
    "균형 분배형" -> "고르게 나누기"
    "수량 후반형" -> "무거운 판 뒤로"
    else -> strategyName
}

private const val REFINED_NOTE =
    " 이건 시작 방식일 뿐이고, 이후 컴퓨터가 점수를 보며 자동으로 더 다듬기 때문에 실제 결과는 이 설명과 조금 다르게 보일 수 있어요."

fun strategyDescription(strategyName: String): String = when (strategyName) {
    "현장 관행형" ->
        "여러 종류가 섞인 판을 1차에 먼저 놓고, 한 종류만 담긴 판은 뒤 차수로 보내는 방식으로 시작해요." + REFINED_NOTE
    "균형 분배형" ->
        "어느 차수에도 판이 몰리지 않도록, 목표한 판 수에 맞춰 차수마다 빈자리부터 채우는 방식으로 시작해요. " +
            "(판의 '개수'를 고르게 맞추는 것이지, 종류나 양을 고르게 맞추는 게 아니에요)" + REFINED_NOTE
    "수량 후반형" ->
        "판에 품목을 담을 때 정한 대·중·소(또는 정확한 개수)를 모두 더해서 판마다 담긴 양을 계산해요. " +
            "이 양이 많은 판일수록 뒤 차수로 보내는 방식으로 시작해요." + REFINED_NOTE
    else -> "배분 방식 설명이 없습니다."
}
