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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.ui.position.MemberUi
import example.yf.fruit_hall.ui.position.PositionUi
import example.yf.fruit_hall.ui.theme.AppTheme

private fun String.toColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) {
    Color(0xFFFFB3C6)
}

private val memberTextColor = Color(0xFF2D2D2D)

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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            localMembers.forEach { member ->
                MemberRow(
                    member = member,
                    isDragging = draggingId == member.id,
                    onToggle = { onToggleWorking(member.id) },
                    onDeleteRequest = {
                        pendingDeleteId = member.id
                        pendingDeleteName = member.name
                    },
                    onDragStarted = { draggingId = member.id },
                    onSwapUp = {
                        val idx = localMembers.indexOfFirst { it.id == member.id }
                        if (idx > 0) {
                            val list = localMembers.toMutableList()
                            list.add(idx - 1, list.removeAt(idx))
                            localMembers = list
                        }
                    },
                    onSwapDown = {
                        val idx = localMembers.indexOfFirst { it.id == member.id }
                        if (idx < localMembers.size - 1) {
                            val list = localMembers.toMutableList()
                            list.add(idx + 1, list.removeAt(idx))
                            localMembers = list
                        }
                    },
                    onDragEnded = {
                        draggingId = null
                        onReorderMembers(localMembers.map { it.id })
                    },
                    onDragCancelled = {
                        draggingId = null
                        localMembers = members
                    }
                )
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
private fun MemberRow(
    member: MemberUi,
    isDragging: Boolean,
    onToggle: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDragStarted: () -> Unit,
    onSwapUp: () -> Unit,
    onSwapDown: () -> Unit,
    onDragEnded: () -> Unit,
    onDragCancelled: () -> Unit
) {
    val memberColor = member.colorHex.toColor()
    val isWorking = member.isWorking

    val swapUpState = rememberUpdatedState(onSwapUp)
    val swapDownState = rememberUpdatedState(onSwapDown)
    val dragEndedState = rememberUpdatedState(onDragEnded)
    val dragCancelledState = rememberUpdatedState(onDragCancelled)

    var accumulatedDrag by remember { mutableStateOf(0f) }
    var rowHeightPx by remember { mutableStateOf(0f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isDragging) 8.dp else 0.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isDragging -> memberColor.copy(alpha = 0.45f)
                    isWorking -> memberColor.copy(alpha = 0.25f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }
            )
            .then(
                if (isWorking && !isDragging)
                    Modifier.border(1.dp, memberColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                else Modifier
            )
            .clickable { onToggle() }
            .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
            .onSizeChanged { rowHeightPx = it.height.toFloat() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = "순서 변경",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDragging) 0.6f else 0.25f),
            modifier = Modifier
                .size(20.dp)
                .pointerInput(member.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            onDragStarted()
                            accumulatedDrag = 0f
                        },
                        onDrag = { _, dragAmount ->
                            accumulatedDrag += dragAmount.y
                            val threshold = rowHeightPx + 6.dp.toPx()
                            while (accumulatedDrag >= threshold) {
                                swapDownState.value()
                                accumulatedDrag -= threshold
                            }
                            while (accumulatedDrag <= -threshold) {
                                swapUpState.value()
                                accumulatedDrag += threshold
                            }
                        },
                        onDragEnd = { dragEndedState.value() },
                        onDragCancel = { dragCancelledState.value() }
                    )
                }
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isWorking) memberColor else memberColor.copy(alpha = 0.35f)),
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
            color = if (isWorking) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isWorking,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(width = 44.dp, height = 24.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = memberColor,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        IconButton(
            onClick = onDeleteRequest,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "삭제",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
