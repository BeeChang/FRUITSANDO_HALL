package example.yf.fruit_hall.ui.component.util

/** 1.0 → "1", 1.53 → "1.5"처럼 소수 첫째 자리로 반올림하고 불필요한 소수점은 생략한다 */
fun Double.toTrimmedDecimalString(): String {
    val rounded = kotlin.math.round(this * 10) / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}
