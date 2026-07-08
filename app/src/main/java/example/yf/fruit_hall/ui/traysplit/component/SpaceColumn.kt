package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.SpaceUi
import example.yf.fruit_hall.ui.traysplit.TrayUi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpaceColumn(
    space: SpaceUi,
    trays: List<TrayUi>,
    currentAssignment: Map<Long, Int>,
    highlightedTrayId: Long?,
    onBoundsChanged: (trayId: Long, Rect) -> Unit,
    onQuickAddTray: () -> Unit,
    onAddTrayDialog: () -> Unit,
    onDeleteSpace: () -> Unit,
    onDeleteTray: (Long) -> Unit,
    onCycleItemSize: (trayId: Long, snackTypeId: Long) -> Unit,
    onRemoveItem: (trayId: Long, snackTypeId: Long) -> Unit,
    onLongPressPin: (trayId: Long) -> Unit,
    onTapRoundBadge: (trayId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Place, contentDescription = null, tint = appColors.primary400)
            Spacer(Modifier.width(6.dp))
            Text(
                text = space.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(appColors.primary100)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${trays.size}판",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary700
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onAddTrayDialog) {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "품목 담아서 판 추가", tint = appColors.grey500)
            }
            IconButton(onClick = onQuickAddTray) {
                Icon(Icons.Default.Add, contentDescription = "판 바로 추가", tint = appColors.primary500)
            }
            IconButton(onClick = onDeleteSpace) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "공간 삭제", tint = appColors.grey400)
            }
        }

        Spacer(Modifier.height(10.dp))

        if (trays.isEmpty()) {
            Text(
                text = "+ 로 판을 바로 추가하세요",
                style = MaterialTheme.typography.bodySmall,
                color = appColors.grey400
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                trays.forEach { tray ->
                    TrayCard(
                        tray = tray,
                        assignedRound = currentAssignment[tray.id],
                        isHighlighted = highlightedTrayId == tray.id,
                        onBoundsChanged = { rect -> onBoundsChanged(tray.id, rect) },
                        onCycleItemSize = { snackTypeId -> onCycleItemSize(tray.id, snackTypeId) },
                        onRemoveItem = { snackTypeId -> onRemoveItem(tray.id, snackTypeId) },
                        onLongPressPin = { onLongPressPin(tray.id) },
                        onTapRoundBadge = { onTapRoundBadge(tray.id) },
                        onDelete = { onDeleteTray(tray.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))
    }
}
