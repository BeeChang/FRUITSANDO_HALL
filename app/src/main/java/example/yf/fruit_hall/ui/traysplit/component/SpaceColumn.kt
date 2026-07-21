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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
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
    onTogglePrimaryLocation: () -> Unit,
    onRenameSpace: () -> Unit,
    needsMoveIds: Set<Long> = emptySet(),
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
            ClickShrinkEffect(onClick = onRenameSpace, shrinkFactor = 0.8f) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.tray_cd_edit_name),
                    tint = appColors.grey400,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            ClickShrinkEffect(onClick = onTogglePrimaryLocation, shrinkFactor = 0.8f) {
                Icon(
                    imageVector = if (space.isPrimaryLocation) Icons.Default.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (space.isPrimaryLocation) stringResource(R.string.tray_cd_primary_location) else stringResource(R.string.tray_cd_set_primary_location),
                    tint = if (space.isPrimaryLocation) appColors.warning500 else appColors.grey400,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(appColors.primary100)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = stringResource(R.string.tray_space_tray_count, trays.size),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary700
                )
            }
            Spacer(Modifier.weight(1f))
            ClickShrinkEffect(onClick = onAddTrayDialog, shrinkFactor = 0.85f) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = stringResource(R.string.tray_cd_add_tray_dialog), tint = appColors.grey500)
                }
            }
            ClickShrinkEffect(onClick = onQuickAddTray, shrinkFactor = 0.85f) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.tray_cd_quick_add_tray), tint = appColors.primary500)
                }
            }
            ClickShrinkEffect(onClick = onDeleteSpace, shrinkFactor = 0.85f) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.tray_cd_delete_space), tint = appColors.grey400)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (trays.isEmpty()) {
            Text(
                text = stringResource(R.string.tray_space_empty_hint),
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
                        needsMove = tray.id in needsMoveIds,
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
