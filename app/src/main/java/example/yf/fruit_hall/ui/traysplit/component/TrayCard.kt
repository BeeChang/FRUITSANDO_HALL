package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.TrayUi

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun TrayCard(
    tray: TrayUi,
    assignedRound: Int?,
    isHighlighted: Boolean,
    needsMove: Boolean = false,
    onBoundsChanged: (Rect) -> Unit,
    onCycleItemSize: (snackTypeId: Long) -> Unit,
    onRemoveItem: (snackTypeId: Long) -> Unit,
    onLongPressPin: () -> Unit,
    onTapRoundBadge: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors

    Card(
        modifier = modifier
            .widthIn(min = 230.dp, max = 260.dp)
            .onGloballyPositioned { onBoundsChanged(it.boundsInWindow()) }
            .then(
                if (isHighlighted) Modifier.border(2.5.dp, appColors.primary400, RoundedCornerShape(18.dp))
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            )
            .combinedClickable(onClick = {}, onLongClick = onLongPressPin),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) appColors.primary400.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(if (isHighlighted) 12.dp else 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                if (tray.pinnedRound != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(appColors.crimson50)
                            .clickable { onLongPressPin() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = stringResource(R.string.tray_card_cd_pinned),
                            tint = appColors.crimson500,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.tray_card_pinned_round_label, tray.pinnedRound),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = appColors.crimson500
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                } else if (assignedRound != null) {
                    val (roundBg, roundText) = roundColorFor(assignedRound)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(roundBg.copy(alpha = 0.28f))
                            .clickable { onTapRoundBadge() }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tray_round_ordinal, assignedRound),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = roundText
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }
                if (tray.pinnedRound == null) {
                    Row(
                        modifier = Modifier.clickable { onLongPressPin() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.tray_pin_round_title),
                            tint = appColors.grey400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.tray_pin_round_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.grey400
                        )
                    }
                }
                if (needsMove) {
                    Spacer(Modifier.width(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(appColors.warning500.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = stringResource(R.string.tray_card_cd_needs_move),
                            tint = appColors.warning500,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.tray_summary_move_needed),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = appColors.warning500
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.tray_card_cd_delete),
                    tint = appColors.grey400,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() }
                )
            }

            Spacer(Modifier.height(8.dp))

            if (tray.items.isEmpty()) {
                Text(
                    text = stringResource(R.string.tray_card_empty_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors.grey400
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tray.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .snackColorBackground(item.colorHex, item.secondaryColorHex, alpha = 0.4f)
                                .clickable { onCycleItemSize(item.snackTypeId) }
                                .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2D2D2D),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.tray_card_cd_remove_item),
                                tint = Color(0xFF2D2D2D).copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(start = 3.dp)
                                    .clickable { onRemoveItem(item.snackTypeId) }
                            )
                        }
                    }
                }
            }
        }
    }
}
