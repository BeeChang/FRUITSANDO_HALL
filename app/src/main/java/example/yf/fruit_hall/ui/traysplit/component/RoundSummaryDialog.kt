package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.RoundSummary
import example.yf.fruit_hall.ui.traysplit.SnackTypeUi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoundSummaryDialog(
    title: String = stringResource(R.string.tray_overall_summary_title),
    summaries: List<RoundSummary>,
    violations: List<String> = emptyList(),
    needsMoveIds: Set<Long> = emptySet(),
    missingItems: List<SnackTypeUi> = emptyList(),
    onSelectCandidate: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var showTextSummary by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.widthIn(max = 820.dp).fillMaxWidth(0.8f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, tint = appColors.white)
                    Spacer(Modifier.width(10.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    ClickShrinkEffect(onClick = { showTextSummary = true }, shrinkFactor = 0.93f) {
                        Text(
                            text = stringResource(R.string.tray_summary_view_result),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = appColors.white,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), tint = appColors.grey300)
                    }
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 560.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    if (missingItems.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = appColors.warning500, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.tray_summary_missing_items_label),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.warning500
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            missingItems.forEach { type ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .border(1.dp, appColors.warning500.copy(alpha = 0.5f), RoundedCornerShape(50))
                                        .snackColorBackground(type.colorHex, type.secondaryColorHex, alpha = 0.25f)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = type.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = androidx.compose.ui.graphics.Color(0xFF2D2D2D)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(16.dp))
                    }

                    summaries.forEachIndexed { index, summary ->
                        val (roundBg, roundText) = roundColorFor(summary.round)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(roundBg.copy(alpha = 0.28f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.tray_round_ordinal, summary.round),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = roundText
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.tray_summary_tray_count_qty, summary.trayCount, summary.approxQty),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.grey600
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        if (summary.items.isEmpty()) {
                            Text(
                                text = stringResource(R.string.tray_summary_no_trays),
                                style = MaterialTheme.typography.labelSmall,
                                color = appColors.grey400
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.tray_summary_all_items_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = appColors.grey500
                            )
                            Spacer(Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                summary.items.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .snackColorBackground(item.colorHex, item.secondaryColorHex, alpha = 0.4f)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = androidx.compose.ui.graphics.Color(0xFF2D2D2D)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.tray_summary_tray_layout_label, summary.trays.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = appColors.grey500
                            )
                            Spacer(Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                summary.trays.forEachIndexed { trayIndex, traySummary ->
                                    Column(
                                        modifier = Modifier
                                            .widthIn(min = 120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = stringResource(R.string.tray_summary_tray_index_label, trayIndex + 1),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = appColors.grey600
                                            )
                                            if (traySummary.trayId in needsMoveIds) {
                                                Spacer(Modifier.width(4.dp))
                                                Box(
                                                    modifier = Modifier.clip(RoundedCornerShape(50))
                                                        .background(appColors.warning500.copy(alpha = 0.2f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.LocalShipping,
                                                            contentDescription = null,
                                                            tint = appColors.warning500,
                                                            modifier = Modifier.height(10.dp)
                                                        )
                                                        Spacer(Modifier.width(2.dp))
                                                        Text(
                                                            stringResource(R.string.tray_summary_move_needed),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = appColors.warning500
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(5.dp))
                                        if (traySummary.items.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.tray_summary_empty_tray),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = appColors.grey400
                                            )
                                        } else {
                                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                traySummary.items.forEach { item ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(50))
                                                            .snackColorBackground(item.colorHex, item.secondaryColorHex, alpha = 0.4f)
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = item.name,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = androidx.compose.ui.graphics.Color(0xFF2D2D2D)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (index != summaries.lastIndex) {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    if (violations.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.tray_summary_violations_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.grey700
                        )
                        Spacer(Modifier.height(8.dp))
                        violations.forEach { violation ->
                            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                Icon(
                                    Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = appColors.warning500,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(violation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onSelectCandidate != null) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.cd_close), color = appColors.grey600)
                        }
                        Button(
                            onClick = onSelectCandidate,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.tray_summary_select_candidate))
                        }
                    } else {
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

    if (showTextSummary) {
        TextSummaryDialog(
            summaries = summaries,
            onDismiss = { showTextSummary = false }
        )
    }
}
