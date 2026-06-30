package example.yf.fruit_hall.ui.calc

data class CalcUiState(
    val expression: String = "",
    val display: String = "0",
    val isResult: Boolean = false
)
