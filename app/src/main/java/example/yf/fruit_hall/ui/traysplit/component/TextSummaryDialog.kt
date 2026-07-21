package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.RoundSummary

/** 차수별 배정을 "1차(볼드) / 품목명 나열" 형식으로 읽고 다른 채널에 옮겨 적을 수 있도록 보여준다. 복사 기능은 없음 */
@Composable
fun TextSummaryDialog(
    summaries: List<RoundSummary>,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(0.55f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                TraySplitDialogHeader(
                    title = stringResource(R.string.tray_summary_view_result),
                    onDismiss = onDismiss
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.tray_text_summary_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.grey500
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        summaries.forEachIndexed { index, summary ->
                            val (roundBg, roundText) = roundColorFor(summary.round)
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(roundBg.copy(alpha = 0.28f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.tray_round_ordinal, summary.round),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = roundText
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            if (summary.items.isEmpty()) {
                                Text(stringResource(R.string.tray_text_summary_no_items), style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                            } else {
                                Text(
                                    text = summary.items.joinToString("    ") { it.name },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (index != summaries.lastIndex) {
                                Spacer(Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                Spacer(Modifier.height(14.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.cd_close))
                    }
                }
            }
        }
    }
}
