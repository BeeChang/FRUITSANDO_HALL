package example.yf.fruit_hall.ui.calc

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class CalcViewModel @Inject constructor() : ViewModel() {

    private var pendingOp: String? = null
    private var operand: Double?   = null

    private val _uiState = MutableStateFlow(CalcUiState())
    val uiState: StateFlow<CalcUiState> = _uiState.asStateFlow()

    fun onDigit(digit: String) {
        val s = _uiState.value
        val newDisplay = when {
            s.isResult                        -> digit
            s.display == "0" && digit != "." -> digit
            digit == "." && "." in s.display -> s.display
            else                              -> s.display + digit
        }
        _uiState.update { it.copy(display = newDisplay, isResult = false) }
    }

    fun onOperator(op: String) {
        val s       = _uiState.value
        val current = s.display.toDoubleOrNull() ?: 0.0
        // 이미 연산자가 대기 중이고 새 숫자가 입력된 경우 먼저 계산하여 연속 연산 지원
        val left = if (pendingOp != null && operand != null && !s.isResult) {
            compute(operand!!, current, pendingOp!!)
        } else {
            current
        }
        operand   = left
        pendingOp = op
        _uiState.update {
            it.copy(expression = "${left.toDisplay()} $op", display = "0", isResult = false)
        }
    }

    fun onEquals() {
        val s  = _uiState.value
        val op = pendingOp ?: return
        val a  = operand   ?: return
        val b  = s.display.toDoubleOrNull() ?: 0.0
        val result = compute(a, b, op)
        pendingOp = null
        operand   = null
        _uiState.update {
            it.copy(
                expression = "${a.toDisplay()} $op ${b.toDisplay()} =",
                display    = result.toDisplay(),
                isResult   = true
            )
        }
    }

    fun onClear() {
        pendingOp      = null
        operand        = null
        _uiState.value = CalcUiState()
    }

    fun onBackspace() {
        val s = _uiState.value
        if (s.isResult) { onClear(); return }
        val new = if (s.display.length <= 1) "0" else s.display.dropLast(1)
        _uiState.update { it.copy(display = new) }
    }

    // 요리 용량 계산 전용: 현재 표시 값에 0.85를 곱한다
    fun onScale085() {
        val num = _uiState.value.display.toDoubleOrNull() ?: return
        val result = num * 0.85
        pendingOp = null
        operand   = null
        _uiState.update {
            it.copy(
                expression = "${num.toDisplay()} × 0.85 =",
                display    = result.toDisplay(),
                isResult   = true
            )
        }
    }

    private fun compute(a: Double, b: Double, op: String): Double = when (op) {
        "+"  -> a + b
        "−"  -> a - b
        "×"  -> a * b
        "÷"  -> if (b != 0.0) a / b else Double.NaN
        else -> b
    }
}

// 부동소수점 오차를 제거하고 불필요한 소수점 이하 0을 제거한다
private fun Double.toDisplay(): String {
    if (isNaN() || isInfinite()) return "오류"
    return BigDecimal.valueOf(this)
        .setScale(8, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}
