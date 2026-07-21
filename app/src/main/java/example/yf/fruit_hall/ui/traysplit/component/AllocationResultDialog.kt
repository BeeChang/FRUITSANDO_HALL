package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.layout.height
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.R
import example.yf.fruit_hall.core.AllocationCandidate
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun AllocationResultDialog(
    attemptCount: Int,
    candidates: List<AllocationCandidate>,
    labels: List<CandidateLabel>,
    onOpenSummary: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var displayedCount by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(attemptCount) {
        val anim = Animatable(0f)
        anim.animateTo(
            targetValue = attemptCount.toFloat(),
            animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing)
        ) {
            displayedCount = value.roundToInt()
        }
        revealed = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(0.55f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                TraySplitDialogHeader(
                    title = stringResource(R.string.tray_result_dialog_title),
                    onDismiss = onDismiss,
                    icon = Icons.Default.Bolt,
                    iconTint = appColors.warning500
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.tray_result_exploration_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.grey600
                        )
                        Text(
                            text = stringResource(R.string.tray_result_exploration_count, displayedCount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = appColors.primary500
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (!revealed) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = appColors.primary400)
                        }
                    }

                    AnimatedVisibility(visible = revealed, enter = fadeIn() + expandVertically()) {
                        Column(
                            modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.tray_result_candidates_hint, candidates.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = appColors.grey500
                            )
                            candidates.forEachIndexed { index, candidate ->
                                CandidateCard(
                                    candidate = candidate,
                                    label = labels.getOrElse(index) {
                                        CandidateLabel.Alternative(index + 1)
                                    }.resolve(),
                                    onClick = { onOpenSummary(index) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.cd_close), color = appColors.grey600)
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateCard(
    candidate: AllocationCandidate,
    label: String,
    onClick: () -> Unit
) {
    val appColors = AppTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.tray_candidate_score_label, candidate.score),
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors.grey500
                )
                Spacer(modifier = Modifier.weight(1f))
                if (candidate.violations.isEmpty()) {
                    Text(stringResource(R.string.tray_candidate_perfect), style = MaterialTheme.typography.labelSmall, color = appColors.success500, fontWeight = FontWeight.Bold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = appColors.warning500, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.tray_candidate_violation_label, candidate.violations.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.warning500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                candidate.roundSizes.forEachIndexed { i, size ->
                    val (roundBg, roundText) = roundColorFor(i + 1)
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(roundBg.copy(alpha = 0.28f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            stringResource(R.string.tray_candidate_round_size_label, i + 1, size),
                            style = MaterialTheme.typography.labelSmall,
                            color = roundText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (candidate.moveCount > 0) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(appColors.warning500.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = appColors.warning500, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                stringResource(R.string.tray_candidate_move_label, candidate.moveCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = appColors.warning500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.tray_candidate_tap_hint), style = MaterialTheme.typography.labelSmall, color = appColors.grey500)
        }
    }
}
