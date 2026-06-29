package example.yf.fruit_hall.ui.position.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import example.yf.fruit_hall.ui.position.MemberUi
import example.yf.fruit_hall.ui.position.PositionUi
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlin.math.roundToInt

private fun String.toColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) {
    Color(0xFFFFB3C6)
}

private val memberTextColor = Color(0xFF2D2D2D)

// Holds drag math values without triggering recomposition on every update
private class DragSession {
    var startIndex: Int = 0
    var cumulativeY: Float = 0f
    var itemStepPx: Float = 0f  // itemHeight + spacingPx
}

@Composable
fun MemberPanel(
    modifier: Modifier = Modifier,
    members: List<MemberUi>,
    memberWeights: Map<Long, Map<Long, Float>>,
    positions: List<PositionUi>,
    onToggleWorking: (Long) -> Unit,
    onManageClick: () -> Unit,
    onDeleteMember: (Long) -> Unit = {},
    onReorderMembers: (List<Long>) -> Unit = {}
) {
    val appColors = AppTheme.colors
    val workingCount = members.count { it.isWorking }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteName by remember { mutableStateOf("") }

    var localMembers by remember { mutableStateOf(members) }
    var draggingId by remember { mutableStateOf<Long?>(null) }

    // floatingOffsetY: Y position of floating card relative to list panel top, in layout pixels
    var floatingOffsetY by remember { mutableStateOf(0f) }
    // panelWindowTop: top of the list Box in window coordinates (boundsInWindow)
    val panelWindowTopRef = remember { FloatArray(1) }

    val session = remember { DragSession() }

    LaunchedEffect(members) {
        if (draggingId == null) localMembers = members
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "멤버",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "출근 $workingCount / 전체 ${members.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "꾹 눌러 순서 변경",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            }
            IconButton(onClick = onManageClick) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "멤버 추가",
                    tint = appColors.primary500
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List area + floating card overlay
        Box(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { panelWindowTopRef[0] = it.boundsInWindow().top }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                localMembers.forEach { member ->
                    // key(member.id): 순서 바뀔 때 composable을 이동(move)시켜
                    // pointerInput 키 변화로 인한 제스처 캔슬을 방지한다
                    key(member.id) {
                        MemberRow(
                            member = member,
                            isPlaceholder = draggingId == member.id,
                            onToggle = { onToggleWorking(member.id) },
                            onDeleteRequest = {
                                pendingDeleteId = member.id
                                pendingDeleteName = member.name
                            },
                            onItemStepMeasured = { stepPx ->
                                if (session.itemStepPx <= 0f) session.itemStepPx = stepPx
                            },
                            onDragStart = { touchWindowY, itemHalfHeight ->
                                session.startIndex = localMembers.indexOfFirst { it.id == member.id }
                                session.cumulativeY = 0f
                                draggingId = member.id
                                floatingOffsetY = touchWindowY - panelWindowTopRef[0] - itemHalfHeight
                            },
                            onDrag = { deltaY ->
                                session.cumulativeY += deltaY
                                floatingOffsetY += deltaY

                                val step = session.itemStepPx
                                if (step > 0f) {
                                    val rawTarget = session.startIndex + (session.cumulativeY / step).roundToInt()
                                    val targetIdx = rawTarget.coerceIn(0, localMembers.size - 1)
                                    val currentIdx = localMembers.indexOfFirst { it.id == draggingId }
                                    if (currentIdx != -1 && targetIdx != currentIdx) {
                                        val list = localMembers.toMutableList()
                                        list.add(targetIdx, list.removeAt(currentIdx))
                                        localMembers = list
                                    }
                                }
                            },
                            onDragEnd = {
                                draggingId = null
                                floatingOffsetY = 0f
                                onReorderMembers(localMembers.map { it.id })
                            },
                            onDragCancel = {
                                draggingId = null
                                floatingOffsetY = 0f
                                localMembers = members
                            }
                        )
                    }
                }
            }

            // Floating ghost card that follows the finger during drag
            if (draggingId != null) {
                val draggedMember = localMembers.find { it.id == draggingId }
                if (draggedMember != null) {
                    FloatingMemberCard(
                        member = draggedMember,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(0, floatingOffsetY.roundToInt().coerceAtLeast(0)) }
                            .zIndex(10f)
                    )
                }
            }
        }
    }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("멤버 삭제") },
            text = { Text("'$pendingDeleteName'을 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteId?.let { onDeleteMember(it) }
                        pendingDeleteId = null
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun FloatingMemberCard(member: MemberUi, modifier: Modifier = Modifier) {
    val memberColor = member.colorHex.toColor()
    Row(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(memberColor.copy(alpha = 0.55f))
            .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = null,
            tint = memberTextColor.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(memberColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.take(1),
                style = MaterialTheme.typography.labelMedium,
                color = memberTextColor,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = member.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MemberRow(
    member: MemberUi,
    isPlaceholder: Boolean,
    onToggle: () -> Unit,
    onDeleteRequest: () -> Unit,
    onItemStepMeasured: (stepPx: Float) -> Unit,
    onDragStart: (touchWindowY: Float, itemHalfHeight: Float) -> Unit,
    onDrag: (deltaY: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val memberColor = member.colorHex.toColor()
    val isWorking = member.isWorking

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
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isPlaceholder -> memberColor.copy(alpha = 0.06f)
                    isWorking -> memberColor.copy(alpha = 0.25f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }
            )
            .then(
                when {
                    isPlaceholder -> Modifier.border(
                        1.5.dp, memberColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)
                    )
                    isWorking -> Modifier.border(
                        1.dp, memberColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)
                    )
                    else -> Modifier
                }
            )
            // 롱프레스+드래그 (clickable보다 먼저 → 롱프레스 시 clickable 차단)
            .pointerInput(member.id) {
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
            // 짧은 탭 → 출근 토글
            .clickable(enabled = !isPlaceholder) { onToggle() }
            .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
            .onSizeChanged { itemHeightRef[0] = it.height.toFloat() }
            .onGloballyPositioned { rowWindowTopRef[0] = it.boundsInWindow().top },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 드래그 가능 시각적 힌트 (인터랙션 없음)
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = null,
            tint = if (isPlaceholder) memberColor.copy(alpha = 0.4f)
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
            modifier = Modifier.size(20.dp)
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (isPlaceholder) memberColor.copy(alpha = 0.2f)
                    else if (isWorking) memberColor
                    else memberColor.copy(alpha = 0.35f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.take(1),
                style = MaterialTheme.typography.labelMedium,
                color = if (isPlaceholder) memberTextColor.copy(alpha = 0.3f) else memberTextColor,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = member.name,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isPlaceholder -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                isWorking -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            },
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isWorking,
            onCheckedChange = { if (!isPlaceholder) onToggle() },
            enabled = !isPlaceholder,
            modifier = Modifier.size(width = 44.dp, height = 24.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = memberColor,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        IconButton(
            onClick = { if (!isPlaceholder) onDeleteRequest() },
            enabled = !isPlaceholder,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "삭제",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isPlaceholder) 0.1f else 0.3f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
