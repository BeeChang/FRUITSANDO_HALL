package example.yf.fruit_hall.ui.position.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import example.yf.fruit_hall.ui.position.DrawResultItem
import example.yf.fruit_hall.ui.position.MemberUi
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultCardGrid(
    drawResult: List<DrawResultItem>,
    onSwap: (fromPositionId: Long, memberId: Long, toPositionId: Long) -> Unit,
    onRedraw: (positionId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleItems = remember { mutableStateMapOf<Int, Boolean>() }
    val cardBounds = remember { mutableStateMapOf<Long, Rect>() }

    var draggedMember by remember { mutableStateOf<MemberUi?>(null) }
    var dragSourcePositionId by remember { mutableStateOf<Long?>(null) }
    var dragWindowPosition by remember { mutableStateOf(Offset.Zero) }
    var highlightedPositionId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(drawResult) {
        visibleItems.clear()
        drawResult.forEachIndexed { index, _ ->
            delay(index * 80L)
            visibleItems[index] = true
        }
    }

    val columns = when {
        drawResult.size <= 2 -> 2
        drawResult.size <= 4 -> 2
        else -> 3
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            itemsIndexed(drawResult, key = { _, item -> item.position.id }) { index, item ->
                AnimatedVisibility(
                    visible = visibleItems[index] == true,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn()
                ) {
                    ResultCard(
                        item = item,
                        isHighlighted = highlightedPositionId == item.position.id,
                        onRedraw = { onRedraw(item.position.id) },
                        onCardBoundsChanged = { bounds -> cardBounds[item.position.id] = bounds },
                        onDragStart = { member, windowPos ->
                            draggedMember = member
                            dragSourcePositionId = item.position.id
                            dragWindowPosition = windowPos
                        },
                        onDrag = { delta ->
                            dragWindowPosition += delta
                            highlightedPositionId = cardBounds.entries
                                .firstOrNull { (_, rect) -> rect.contains(dragWindowPosition) }
                                ?.key
                                ?.takeIf { it != dragSourcePositionId }
                        },
                        onDragEnd = {
                            val target = highlightedPositionId
                            val member = draggedMember
                            val source = dragSourcePositionId
                            if (target != null && member != null && source != null && target != source) {
                                onSwap(source, member.id, target)
                            }
                            draggedMember = null
                            dragSourcePositionId = null
                            highlightedPositionId = null
                            dragWindowPosition = Offset.Zero
                        }
                    )
                }
            }
        }

        if (draggedMember != null) {
            SuggestionChip(
                onClick = {},
                label = { Text(draggedMember?.name ?: "") },
                modifier = Modifier
                    .offset {
                        IntOffset(
                            dragWindowPosition.x.roundToInt() - 40,
                            dragWindowPosition.y.roundToInt() - 20
                        )
                    }
                    .zIndex(10f),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    labelColor = MaterialTheme.colorScheme.onTertiary
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultCard(
    item: DrawResultItem,
    isHighlighted: Boolean,
    onRedraw: () -> Unit,
    onCardBoundsChanged: (Rect) -> Unit,
    onDragStart: (MemberUi, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    val accentColors = listOf(
        0xFF1976D2.toInt(),
        0xFF388E3C.toInt(),
        0xFFF57C00.toInt(),
        0xFF7B1FA2.toInt(),
        0xFFC62828.toInt(),
        0xFF00838F.toInt()
    )
    val accentColor = androidx.compose.ui.graphics.Color(
        accentColors[item.position.sortOrder % accentColors.size]
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords -> onCardBoundsChanged(coords.boundsInWindow()) }
            .border(
                width = if (isHighlighted) 2.dp else 0.dp,
                color = if (isHighlighted) MaterialTheme.colorScheme.tertiary
                else androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted)
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.position.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                IconButton(
                    onClick = onRedraw,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "다시뽑기",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (item.members.isEmpty()) {
                Text(
                    text = "배정 없음",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.members.forEach { member ->
                        DraggableMemberChip(
                            member = member,
                            onDragStart = { windowPos -> onDragStart(member, windowPos) },
                            onDrag = onDrag,
                            onDragEnd = onDragEnd
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableMemberChip(
    member: MemberUi,
    onDragStart: (windowPos: Offset) -> Unit,
    onDrag: (delta: Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var chipWindowBounds by remember { mutableStateOf(Rect.Zero) }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        modifier = Modifier
            .onGloballyPositioned { coords ->
                chipWindowBounds = coords.boundsInWindow()
            }
            .pointerInput(member.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        val windowPos = Offset(
                            chipWindowBounds.left + localOffset.x,
                            chipWindowBounds.top + localOffset.y
                        )
                        onDragStart(windowPos)
                    },
                    onDrag = { _, dragAmount ->
                        onDrag(dragAmount)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
}
