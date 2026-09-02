package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.yf.fruit_hall.R
import example.yf.fruit_hall.core.rotation.CellState
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.rotation.RotationCellUi
import example.yf.fruit_hall.ui.rotation.RotationRowUi
import example.yf.fruit_hall.ui.rotation.RotationSlotUi
import example.yf.fruit_hall.ui.rotation.SelectedCell
import example.yf.fruit_hall.ui.rotation.formatMinutes
import example.yf.fruit_hall.ui.theme.AppTheme

private val CELL_WIDTH = 76.dp
private val NAME_WIDTH = 64.dp
private val CELL_HEIGHT = 56.dp

private fun String?.toColorOrNull(): Color? = try {
    this?.let { Color(android.graphics.Color.parseColor(it)) }
} catch (e: Exception) {
    null
}

@Composable
fun RotationGridPanel(
    slots: List<RotationSlotUi>,
    rows: List<RotationRowUi>,
    selectedCell: SelectedCell?,
    onTapCell: (slotIndex: Int, memberId: Long) -> Unit,
    onTogglePin: (slotIndex: Int, memberId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    if (slots.isEmpty() || rows.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.rotation_grid_empty_hint), color = appColors.grey600)
        }
        return
    }

    Box(modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            // 헤더: 슬롯 시각
            Row {
                Box(Modifier.width(NAME_WIDTH).height(CELL_HEIGHT), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.rotation_grid_name_header), style = MaterialTheme.typography.labelSmall)
                }
                slots.forEach { slot ->
                    Box(
                        Modifier.width(CELL_WIDTH).height(CELL_HEIGHT)
                            .background(if (slot.isFrozen) appColors.grey200 else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(formatMinutes(slot.startMin), fontSize = 11.sp)
                            Text(formatMinutes(slot.endMin), fontSize = 11.sp, color = appColors.grey600)
                        }
                    }
                }
            }

            rows.forEach { row ->
                Row {
                    Box(
                        Modifier.width(NAME_WIDTH).height(CELL_HEIGHT)
                            .background((row.colorHex.toColorOrNull() ?: appColors.grey100).copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(row.name, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                    }
                    row.cells.forEach { cell ->
                        RotationCell(
                            cell = cell,
                            isSelected = selectedCell?.slotIndex == cell.slotIndex && selectedCell.memberId == row.memberId,
                            onTap = { onTapCell(cell.slotIndex, row.memberId) },
                            onTogglePin = { onTogglePin(cell.slotIndex, row.memberId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RotationCell(
    cell: RotationCellUi,
    isSelected: Boolean,
    onTap: () -> Unit,
    onTogglePin: () -> Unit
) {
    val appColors = AppTheme.colors
    val bg = when (cell.state) {
        CellState.ASSIGNED -> cell.colorHex.toColorOrNull() ?: appColors.grey200
        CellState.BREAK -> appColors.grey300
        CellState.OFF -> Color.Transparent
    }
    val border = when {
        isSelected -> appColors.primary700
        cell.hasViolation -> appColors.crimson500
        else -> Color.Transparent
    }

    Box(
        Modifier
            .width(CELL_WIDTH).height(CELL_HEIGHT)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg.copy(alpha = if (cell.state == CellState.OFF) 0f else 0.55f))
            .border(2.dp, border, RoundedCornerShape(8.dp))
    ) {
        if (cell.state == CellState.ASSIGNED) {
            ClickShrinkEffect(modifier = Modifier.size(CELL_WIDTH, CELL_HEIGHT), onClick = onTap) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(cell.positionName ?: "-", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                }
            }
            ClickShrinkEffect(
                modifier = Modifier.align(Alignment.TopEnd).size(18.dp),
                onClick = onTogglePin
            ) {
                Icon(
                    if (cell.isPinned) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = stringResource(R.string.rotation_grid_pin_cd),
                    modifier = Modifier.size(14.dp),
                    tint = if (cell.isPinned) appColors.primary700 else appColors.grey600.copy(alpha = 0.4f)
                )
            }
        } else if (cell.state == CellState.BREAK) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.rotation_grid_break_label), style = MaterialTheme.typography.labelSmall, color = appColors.grey700)
            }
        }
    }
}
