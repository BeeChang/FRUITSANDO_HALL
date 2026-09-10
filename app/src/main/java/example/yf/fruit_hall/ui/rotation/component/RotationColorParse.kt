package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.ui.graphics.Color

// 로테이션 탭의 색은 전부 DB에 "#RRGGBB" 문자열로 들어 있다. 사용자가 만든 값이라 비어 있거나
// 깨져 있을 수 있어서 파싱은 반드시 실패를 흡수해야 한다 — 여기 한 곳에서만 처리한다.
// (예전에는 컴포넌트 6곳이 같은 try/catch를 각자 들고 있었고, 대비색만 제각각이었다.)

/** 파싱할 수 없으면(빈 문자열·깨진 값) null. 대비색을 호출부가 직접 고르고 싶을 때 쓴다. */
internal fun String?.toRotationColorOrNull(): Color? = try {
    this?.takeIf { it.isNotBlank() }?.let { Color(android.graphics.Color.parseColor(it)) }
} catch (e: IllegalArgumentException) {
    null
}

/** 파싱할 수 없으면 [fallback]. 색이 없으면 칩 자체가 안 보이는 자리에서 쓴다. */
internal fun String?.toRotationColor(fallback: Color): Color = toRotationColorOrNull() ?: fallback

/** 멤버·포지션 칩의 기본 분홍. 색을 아직 안 고른 항목이 이 색으로 나온다. */
internal val rotationDefaultChipColor = Color(0xFFFFB3C6)

/** 파스텔 칩 위에 얹는 글자색. 칩 배경이 밝은 계열로 고정이라 다크 테마에서도 이 값이어야 읽힌다. */
internal val rotationChipTextColor = Color(0xFF2D2D2D)
