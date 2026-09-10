package example.yf.fruit_hall.ui.rotation

fun formatMinutes(min: Int): String {
    val h = (min / 60).coerceIn(0, 23)
    val m = min % 60
    return "%02d:%02d".format(h, m)
}

/** "HH:mm" 문자열을 분으로. 형식이 틀리면 null. */
fun parseMinutes(text: String): Int? {
    val parts = text.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..29 || m !in 0..59) return null
    return h * 60 + m
}
