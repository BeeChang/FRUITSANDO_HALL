package example.yf.fruit_hall.ui.position.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.ui.position.MemberUi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrawAnimationPanel(
    members: List<MemberUi>,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "drum")

    val memberOffsets = remember(members) {
        members.mapIndexed { index, _ ->
            val duration = 600 + (index * 97 % 621)
            val initialOffset = (-30 + (index * 13 % 61)).toFloat()
            duration to initialOffset
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "뽑는 중...",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(percent = 40)
                    )
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(percent = 40)
                    )
                    .padding(horizontal = 48.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4
                ) {
                    members.forEachIndexed { index, member ->
                        val (duration, initialOffset) = memberOffsets[index]

                        val animatedFloat by infiniteTransition.animateFloat(
                            initialValue = initialOffset,
                            targetValue = initialOffset + 40f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = duration, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "float_$index"
                        )

                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier.offset(y = animatedFloat.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }

        Button(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Text("건너뛰기")
        }
    }
}
