package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.ui.graphics.Color

/** 차수별 구분 색(배경 연한 톤, 텍스트 진한 톤). 차수가 팔레트보다 많으면 순환한다. */
private val roundAccentColors: List<Pair<Color, Color>> = listOf(
    Color(0xFF6196FD) to Color(0xFF1565A8), // 1차 블루
    Color(0xFFFF9BB5) to Color(0xFFB02060), // 2차 핑크
    Color(0xFF66E9AC) to Color(0xFF1A7A52), // 3차 그린
    Color(0xFFFFD580) to Color(0xFF8C6200), // 4차 앰버
    Color(0xFFD4B8F5) to Color(0xFF6A3D9A), // 5차 퍼플
    Color(0xFF9FD8D8) to Color(0xFF1F5473), // 6차 티얼
    Color(0xFFFFA8C0) to Color(0xFF8E0000), // 7차 로즈
    Color(0xFFC7F2D4) to Color(0xFF006D3A), // 8차 민트
)

fun roundColorFor(round: Int): Pair<Color, Color> =
    roundAccentColors[(round - 1).mod(roundAccentColors.size)]
