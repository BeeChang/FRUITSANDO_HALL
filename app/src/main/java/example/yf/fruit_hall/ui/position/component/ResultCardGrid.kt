package example.yf.fruit_hall.ui.position.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import example.yf.fruit_hall.ui.position.DrawResultItem
import example.yf.fruit_hall.ui.position.MemberUi
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private fun String.toColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) {
    Color(0xFFFFB3C6)
}

private val chipTextColor = Color(0xFF2D2D2D)

private val positionAccentColors = listOf(
    Color(0xFF6196FD),  // 소프트 블루
    Color(0xFFE05F80),  // 소프트 로즈
    Color(0xFF45B07A),  // 소프트 그린
    Color(0xFFE8A040),  // 소프트 앰버
    Color(0xFF8B70C8),  // 소프트 퍼플
    Color(0xFF40A8C8),  // 소프트 티얼
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultCardGrid(
    drawResult: List<DrawResultItem>,
    onSwap: (fromPositionId: Long, memberId: Long, toPositionId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleItems = remember { mutableStateMapOf<Int, Boolean>() }
    val cardBounds = remember { mutableStateMapOf<Long, Rect>() }

    var draggedMember by remember { mutableStateOf<MemberUi?>(null) }
    var dragSourcePositionId by remember { mutableStateOf<Long?>(null) }
    var dragWindowPosition by remember { mutableStateOf(Offset.Zero) }
    var highlightedPositionId by remember { mutableStateOf<Long?>(null) }
    var boxBoundsInWindow by remember { mutableStateOf(Rect.Zero) }

    LaunchedEffect(drawResult) {
        visibleItems.clear()
        drawResult.forEachIndexed { index, _ ->
            delay(index * 80L)
            visibleItems[index] = true
        }
    }

    val columns = if (drawResult.size <= 4) 2 else 3

    Box(modifier = modifier.fillMaxSize().onGloballyPositioned { boxBoundsInWindow = it.boundsInWindow() }) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            userScrollEnabled = draggedMember == null
        ) {
            itemsIndexed(drawResult, key = { _, item -> item.position.id }) { index, item ->
                val accentColor = positionAccentColors[item.position.sortOrder % positionAccentColors.size]
                AnimatedVisibility(
                    visible = visibleItems[index] == true,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn()
                ) {
                    ResultCard(
                        item = item,
                        accentColor = accentColor,
                        isHighlighted = highlightedPositionId == item.position.id,
                        onCardBoundsChanged = { bounds -> cardBounds[item.position.id] = bounds },
                        onChipDragStart = { member, windowPos ->
                            draggedMember = member
                            dragSourcePositionId = item.position.id
                            dragWindowPosition = windowPos
                        },
                        onChipDrag = { delta ->
                            dragWindowPosition += delta
                            highlightedPositionId = cardBounds.entries
                                .firstOrNull { (_, rect) -> rect.contains(dragWindowPosition) }
                                ?.key
                                ?.takeIf { it != dragSourcePositionId }
                        },
                        onChipDragEnd = {
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
            val chipColor = draggedMember?.colorHex?.toColor() ?: Color(0xFFFFB3C6)
            Row(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (dragWindowPosition.x - boxBoundsInWindow.left).roundToInt() - 60,
                            (dragWindowPosition.y - boxBoundsInWindow.top).roundToInt() - 18
                        )
                    }
                    .zIndex(100f)
                    .shadow(12.dp, RoundedCornerShape(50))
                    .background(chipColor.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = null,
                    tint = chipTextColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = draggedMember?.name ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = chipTextColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultCard(
    item: DrawResultItem,
    accentColor: Color,
    isHighlighted: Boolean,
    onCardBoundsChanged: (Rect) -> Unit,
    onChipDragStart: (MemberUi, Offset) -> Unit,
    onChipDrag: (Offset) -> Unit,
    onChipDragEnd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isHighlighted) 16.dp else 4.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .onGloballyPositioned { coords -> onCardBoundsChanged(coords.boundsInWindow()) }
            .then(
                if (isHighlighted) Modifier.border(2.5.dp, accentColor, RoundedCornerShape(20.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted)
                accentColor.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.position.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.members.isEmpty()) {
                    Text(
                        text = "배정 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "꾹 눌러서 이동",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item.members.forEach { member ->
                            DraggableMemberChip(
                                member = member,
                                onDragStart = { windowPos -> onChipDragStart(member, windowPos) },
                                onDrag = onChipDrag,
                                onDragEnd = onChipDragEnd
                            )
                        }
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
    val memberColor = member.colorHex.toColor()
    var chipWindowBounds by remember { mutableStateOf(Rect.Zero) }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(memberColor.copy(alpha = 0.35f))
            .onGloballyPositioned { coords -> chipWindowBounds = coords.boundsInWindow() }
            .pointerInput(member.id) {
                detectDragGestures(
                    onDragStart = { localOffset ->
                        onDragStart(
                            Offset(
                                chipWindowBounds.left + localOffset.x,
                                chipWindowBounds.top + localOffset.y
                            )
                        )
                    },
                    onDrag = { _, dragAmount -> onDrag(dragAmount) },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .padding(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = null,
            tint = chipTextColor.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = member.name,
            style = MaterialTheme.typography.bodyMedium,
            color = chipTextColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
