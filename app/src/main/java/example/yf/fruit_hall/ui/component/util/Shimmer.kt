package example.yf.fruit_hall.ui.component.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.shimmer(
   radius: Dp = 12.dp,
   colorList: List<Color> = listOf(
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
      MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
   ),
   ratio: Float = 0.4f,
): Modifier = composed {
   val transition = rememberInfiniteTransition(label = "shimmer")
   val density = LocalDensity.current
   val configuration = LocalConfiguration.current
   val screenWidthDp = configuration.screenWidthDp.dp
   val shimmerWidth = screenWidthDp * ratio
   val targetValue = with(density) { (screenWidthDp + shimmerWidth).toPx() }

   val translateAnim by transition.animateFloat(
      initialValue = 0f,
      targetValue = targetValue,
      animationSpec = infiniteRepeatable(
         animation = tween(
            durationMillis = 1100,
            easing = FastOutSlowInEasing
         ),
         repeatMode = RepeatMode.Restart
      ),
      label = "shimmer_translate"
   )

   clip(RoundedCornerShape(radius))
      .background(
         brush = Brush.linearGradient(
            colors = colorList,
            start = Offset(
               x = translateAnim - shimmerWidth.value,
               y = translateAnim - shimmerWidth.value
            ),
            end = Offset(x = translateAnim, y = translateAnim)
         )
      )
}