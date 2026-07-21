package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.SnackTypeUi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SnackChipPalette(
    snackTypes: List<SnackTypeUi>,
    onManageClick: () -> Unit,
    onChipDragStart: (SnackTypeUi, windowPos: Offset) -> Unit,
    onChipDrag: (delta: Offset) -> Unit,
    onChipDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Default.Icecream, contentDescription = null, tint = appColors.grey600, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.tray_palette_item_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            ClickShrinkEffect(onClick = onManageClick, shrinkFactor = 0.93f) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(appColors.primary100)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tray_palette_manage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = appColors.primary700
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.tray_palette_drag_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(8.dp))

        if (snackTypes.isEmpty()) {
            Text(
                text = stringResource(R.string.tray_palette_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = appColors.grey400
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                snackTypes.forEach { type ->
                    DraggableSnackChip(
                        type = type,
                        onDragStart = { pos -> onChipDragStart(type, pos) },
                        onDrag = onChipDrag,
                        onDragEnd = onChipDragEnd
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableSnackChip(
    type: SnackTypeUi,
    onDragStart: (windowPos: Offset) -> Unit,
    onDrag: (delta: Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var chipWindowBounds by remember { mutableStateOf(Rect.Zero) }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .snackColorBackground(type.colorHex, type.secondaryColorHex, alpha = 0.4f)
            .onGloballyPositioned { chipWindowBounds = it.boundsInWindow() }
            // 롱프레스 없이 즉시 드래그 시작 (체감 속도 최우선)
            .pointerInput(type.id) {
                detectDragGestures(
                    onDragStart = { localOffset ->
                        onDragStart(Offset(chipWindowBounds.left + localOffset.x, chipWindowBounds.top + localOffset.y))
                    },
                    onDrag = { _, delta -> onDrag(delta) },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .padding(start = 10.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).snackColorBackground(type.colorHex, type.secondaryColorHex))
        Spacer(Modifier.width(6.dp))
        Text(
            text = type.name,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF2D2D2D),
            fontWeight = FontWeight.SemiBold
        )
    }
}
