package example.yf.fruit_hall.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LightColorScheme = lightColorScheme(
    primary = MyColor.primary500,
    onPrimary = MyColor.white,
    primaryContainer = MyColor.primary100,
    onPrimaryContainer = MyColor.primary900,

    secondary = MyColor.secondary500,
    onSecondary = MyColor.white,
    secondaryContainer = MyColor.secondary100,
    onSecondaryContainer = MyColor.secondary900,

    error = MyColor.crimson500,
    onError = MyColor.white,
    errorContainer = MyColor.crimson100,
    onErrorContainer = MyColor.crimson900,

    background = MyColor.white,
    onBackground = MyColor.black,

    surface = MyColor.grey20,
    onSurface = MyColor.black,
    surfaceVariant = MyColor.grey100,
    onSurfaceVariant = MyColor.grey700,

    outline = MyColor.grey300,
    outlineVariant = MyColor.grey200,
)

object AppTheme {
    val colors: AppColor
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

@Composable
fun FruitHallTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppColors provides MyColor) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}