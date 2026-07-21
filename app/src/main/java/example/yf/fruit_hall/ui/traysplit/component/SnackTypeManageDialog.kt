package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.SnackTypeUi
import kotlin.math.roundToInt

// 물품 칩용 파스텔 팔레트
val snackColorPalette = listOf(
    "#FFB3C6", "#FFD6A5", "#FFEAA7", "#B5EAD7",
    "#A8E6CF", "#B3D9FF", "#A8D0F8", "#D4B8F5",
    "#C9B2F0", "#F0C0D8", "#9FD8D8", "#FFD0A8",
)

private const val DARK_TEXT = 0xFF2D2D2D

fun String.toSnackColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) {
    Color(0xFFFFB3C6)
}

/** 단색이면 그대로 채우고, secondaryHex가 있으면 두 색을 그라데이션으로 섞어 채운다 (품목 2색 혼합 표시) */
fun Modifier.snackColorBackground(primaryHex: String, secondaryHex: String?, alpha: Float = 1f): Modifier {
    val c1 = primaryHex.toSnackColor().copy(alpha = alpha)
    return if (secondaryHex == null) {
        this.background(c1)
    } else {
        val c2 = secondaryHex.toSnackColor().copy(alpha = alpha)
        this.drawBehind {
            drawRect(brush = Brush.linearGradient(colors = listOf(c1, c2)))
        }
    }
}

private class DragSession {
    var startIndex: Int = 0
    var cumulativeY: Float = 0f
    var itemStepPx: Float = 0f
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SnackTypeManageDialog(
    snackTypes: List<SnackTypeUi>,
    onAdd: (name: String, colorHex: String, secondaryColorHex: String?) -> Unit,
    onDelete: (Long) -> Unit,
    onReorder: (List<Long>) -> Unit,
    onToggleActive: (id: Long, isActive: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var newName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(snackColorPalette[0]) }
    var mixEnabled by remember { mutableStateOf(false) }
    var selectedSecondaryColor by remember { mutableStateOf(snackColorPalette[1]) }

    var localSnackTypes by remember { mutableStateOf(snackTypes) }
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var floatingOffsetY by remember { mutableStateOf(0f) }
    val panelWindowTopRef = remember { FloatArray(1) }
    val session = remember { DragSession() }

    LaunchedEffect(snackTypes) {
        if (draggingId == null) localSnackTypes = snackTypes
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.55f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Icecream,
                        contentDescription = null,
                        tint = appColors.white,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.tray_manage_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.white,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = appColors.grey300,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    if (localSnackTypes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.tray_manage_empty_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.grey400
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.tray_manage_reorder_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.grey400
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .heightIn(max = 220.dp)
                                .onGloballyPositioned { panelWindowTopRef[0] = it.boundsInWindow().top }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                localSnackTypes.forEach { type ->
                                    key(type.id) {
                                        SnackTypeRow(
                                            type = type,
                                            isPlaceholder = draggingId == type.id,
                                            onDelete = { onDelete(type.id) },
                                            onToggleActive = { onToggleActive(type.id, it) },
                                            onItemStepMeasured = { step -> if (session.itemStepPx <= 0f) session.itemStepPx = step },
                                            onDragStart = { touchWindowY, halfHeight ->
                                                session.startIndex = localSnackTypes.indexOfFirst { it.id == type.id }
                                                session.cumulativeY = 0f
                                                draggingId = type.id
                                                floatingOffsetY = touchWindowY - panelWindowTopRef[0] - halfHeight
                                            },
                                            onDrag = { deltaY ->
                                                session.cumulativeY += deltaY
                                                floatingOffsetY += deltaY
                                                val step = session.itemStepPx
                                                if (step > 0f) {
                                                    val rawTarget = session.startIndex + (session.cumulativeY / step).roundToInt()
                                                    val targetIdx = rawTarget.coerceIn(0, localSnackTypes.size - 1)
                                                    val currentIdx = localSnackTypes.indexOfFirst { it.id == draggingId }
                                                    if (currentIdx != -1 && targetIdx != currentIdx) {
                                                        val list = localSnackTypes.toMutableList()
                                                        list.add(targetIdx, list.removeAt(currentIdx))
                                                        localSnackTypes = list
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                draggingId = null
                                                floatingOffsetY = 0f
                                                onReorder(localSnackTypes.map { it.id })
                                            },
                                            onDragCancel = {
                                                draggingId = null
                                                floatingOffsetY = 0f
                                                localSnackTypes = snackTypes
                                            }
                                        )
                                    }
                                }
                            }

                            if (draggingId != null) {
                                val draggedType = localSnackTypes.find { it.id == draggingId }
                                if (draggedType != null) {
                                    FloatingSnackRow(
                                        type = draggedType,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset { IntOffset(0, floatingOffsetY.roundToInt().coerceAtLeast(0)) }
                                            .zIndex(10f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.tray_manage_add_new_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = appColors.grey700,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.tray_manage_name_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = selectedColor.toSnackColor(),
                            focusedLabelColor = appColors.grey600
                        ),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .snackColorBackground(selectedColor, if (mixEnabled) selectedSecondaryColor else null)
                            )
                        }
                    )

                    Spacer(Modifier.height(14.dp))
                    Text(text = stringResource(R.string.tray_manage_color1_label), style = MaterialTheme.typography.labelSmall, color = appColors.grey500)
                    Spacer(Modifier.height(8.dp))

                    ColorSwatchGrid(selected = selectedColor, onSelect = { selectedColor = it })

                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.tray_manage_mix_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = appColors.grey600,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = mixEnabled, onCheckedChange = { mixEnabled = it })
                    }

                    if (mixEnabled) {
                        Spacer(Modifier.height(10.dp))
                        Text(text = stringResource(R.string.tray_manage_color2_label), style = MaterialTheme.typography.labelSmall, color = appColors.grey500)
                        Spacer(Modifier.height(8.dp))
                        ColorSwatchGrid(selected = selectedSecondaryColor, onSelect = { selectedSecondaryColor = it })
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.cd_close), color = appColors.grey600)
                        }
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    onAdd(newName.trim(), selectedColor, if (mixEnabled) selectedSecondaryColor else null)
                                    newName = ""
                                }
                            },
                            enabled = newName.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.add))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSwatchGrid(selected: String, onSelect: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        snackColorPalette.forEach { hex ->
            val isSelected = selected == hex
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(hex.toSnackColor())
                    .then(
                        if (isSelected) Modifier.border(BorderStroke(2.5.dp, Color(DARK_TEXT)), CircleShape)
                        else Modifier
                    )
                    .clickable { onSelect(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(DARK_TEXT),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SnackTypeRow(
    type: SnackTypeUi,
    isPlaceholder: Boolean,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onItemStepMeasured: (stepPx: Float) -> Unit,
    onDragStart: (touchWindowY: Float, itemHalfHeight: Float) -> Unit,
    onDrag: (deltaY: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val appColors = AppTheme.colors
    val dragStartState = rememberUpdatedState(onDragStart)
    val dragState = rememberUpdatedState(onDrag)
    val dragEndState = rememberUpdatedState(onDragEnd)
    val dragCancelState = rememberUpdatedState(onDragCancel)
    val stepMeasuredState = rememberUpdatedState(onItemStepMeasured)
    val itemHeightRef = remember { FloatArray(1) }
    val rowWindowTopRef = remember { FloatArray(1) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .snackColorBackground(type.colorHex, type.secondaryColorHex, alpha = if (isPlaceholder) 0.06f else 0.25f)
            .pointerInput(type.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        val touchWindowY = rowWindowTopRef[0] + localOffset.y
                        val halfHeight = itemHeightRef[0] / 2f
                        stepMeasuredState.value(itemHeightRef[0] + 6.dp.toPx())
                        dragStartState.value(touchWindowY, halfHeight)
                    },
                    onDrag = { _, delta -> dragState.value(delta.y) },
                    onDragEnd = { dragEndState.value() },
                    onDragCancel = { dragCancelState.value() }
                )
            }
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
            .onSizeChanged { itemHeightRef[0] = it.height.toFloat() }
            .onGloballyPositioned { rowWindowTopRef[0] = it.boundsInWindow().top },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = null,
            tint = Color(DARK_TEXT).copy(alpha = if (isPlaceholder) 0.15f else 0.35f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .snackColorBackground(type.colorHex, type.secondaryColorHex, alpha = if (isPlaceholder) 0.25f else 1f)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = type.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = Color(DARK_TEXT).copy(alpha = if (isPlaceholder) 0.25f else if (type.isActive) 1f else 0.4f),
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = type.isActive,
            onCheckedChange = onToggleActive,
            enabled = !isPlaceholder,
            modifier = Modifier.height(28.dp)
        )
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete, enabled = !isPlaceholder, modifier = Modifier.size(34.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.tray_cd_delete),
                tint = appColors.crimson400.copy(alpha = if (isPlaceholder) 0.1f else 0.6f),
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun FloatingSnackRow(type: SnackTypeUi, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .snackColorBackground(type.colorHex, type.secondaryColorHex, alpha = 0.55f)
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = null,
            tint = Color(DARK_TEXT).copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).snackColorBackground(type.colorHex, type.secondaryColorHex))
        Spacer(Modifier.width(10.dp))
        Text(
            text = type.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(DARK_TEXT),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}
