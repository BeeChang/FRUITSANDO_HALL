package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import example.yf.fruit_hall.ui.rotation.MemberUi
import example.yf.fruit_hall.ui.rotation.RoleAssignmentUi
import example.yf.fruit_hall.ui.rotation.RotationPositionUi
import example.yf.fruit_hall.ui.rotation.RotationRoleUi
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlin.math.roundToInt

private fun String.toColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) {
    Color(0xFFFFB3C6)
}

private val chipTextColor = Color(0xFF2D2D2D)
private val roleFallbackPalette = listOf(Color(0xFF6196FD), Color(0xFFE05F80), Color(0xFF45B07A), Color(0xFFE8A040), Color(0xFF8B70C8), Color(0xFF40A8C8))

/** 화면 안에 상시 붙는 인원배정 패널. 왼쪽은 미배정 멤버, 오른쪽은 근무 스케줄 카드 — 드래그로 배정한다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberAssignPanel(
    roles: List<RotationRoleUi>,
    positions: List<RotationPositionUi>,
    assignments: List<RoleAssignmentUi>,
    members: List<MemberUi>,
    onAssign: (roleLabel: String, memberId: Long?) -> Unit,
    onUpdateTime: (roleLabel: String, startMin: Int, endMin: Int) -> Unit,
    onToggleRoleActive: (Long, Boolean) -> Unit,
    onOpenMemberManage: () -> Unit,
    onOpenPresetManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    val assignmentByLabel = assignments.associateBy { it.roleLabel }
    val assignedMemberIds = assignments.mapNotNull { it.memberId }.toSet()
    val unassignedMembers = members.filter { it.id !in assignedMemberIds }
    val sortedRoles = roles.sortedBy { it.sortOrder }
    val positionColorByName = positions.associate { it.name to it.colorHex.toColor() }

    val cardBounds = remember { mutableStateMapOf<String, Rect>() }
    var draggedMember by remember { mutableStateOf<MemberUi?>(null) }
    var dragSourceRoleLabel by remember { mutableStateOf<String?>(null) }
    var dragWindowPosition by remember { mutableStateOf(Offset.Zero) }
    var highlightedRoleLabel by remember { mutableStateOf<String?>(null) }
    var boxBoundsInWindow by remember { mutableStateOf(Rect.Zero) }

    fun handleDragStart(member: MemberUi, sourceRoleLabel: String?, windowPos: Offset) {
        draggedMember = member
        dragSourceRoleLabel = sourceRoleLabel
        dragWindowPosition = windowPos
    }

    fun handleDrag(delta: Offset) {
        dragWindowPosition += delta
        highlightedRoleLabel = cardBounds.entries
            .firstOrNull { (_, rect) -> rect.contains(dragWindowPosition) }
            ?.key
            ?.takeIf { it != dragSourceRoleLabel }
    }

    fun handleDragEnd() {
        val target = highlightedRoleLabel
        val member = draggedMember
        val source = dragSourceRoleLabel
        if (target != null && member != null) {
            if (source != null) {
                val targetPrevMemberId = assignmentByLabel[target]?.memberId
                onAssign(source, targetPrevMemberId)
            }
            onAssign(target, member.id)
        }
        draggedMember = null
        dragSourceRoleLabel = null
        highlightedRoleLabel = null
        dragWindowPosition = Offset.Zero
    }

    Row(modifier.background(MaterialTheme.colorScheme.background)) {
        // 왼쪽: 미배정 멤버 (세로 목록)
        Column(
            Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "미배정 멤버", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenMemberManage, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "멤버 추가·수정", tint = appColors.primary500, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                "${unassignedMembers.size}명 · 꾹 눌러 끌어서 오른쪽 포지션에 놓으세요",
                style = MaterialTheme.typography.labelSmall, color = appColors.grey500,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (members.isEmpty()) {
                    EmptyStateButton(text = "멤버가 없습니다 · 추가하기", onClick = onOpenMemberManage, color = appColors.primary500)
                } else if (unassignedMembers.isEmpty()) {
                    Text("모두 배정됨", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                } else {
                    unassignedMembers.forEach { member ->
                        DraggableMemberChip(
                            member = member,
                            onDragStart = { windowPos -> handleDragStart(member, null, windowPos) },
                            onDrag = { delta -> handleDrag(delta) },
                            onDragEnd = { handleDragEnd() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // 오른쪽: 근무 스케줄 카드
        Box(
            Modifier.weight(1f).fillMaxHeight()
                .onGloballyPositioned { boxBoundsInWindow = it.boundsInWindow() }
        ) {
            if (sortedRoles.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    EmptyStateButton(
                        text = "근무 스케줄이 없습니다 · 프리셋 관리 열기", onClick = onOpenPresetManage, color = appColors.primary500,
                        icon = Icons.Default.Add
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    sortedRoles.forEachIndexed { index, role ->
                        val assignment = assignmentByLabel[role.label]
                        val assignedMember = assignment?.memberId?.let { id -> members.firstOrNull { it.id == id } }
                        val accent = positionColorByName[role.label] ?: roleFallbackPalette[index % roleFallbackPalette.size]
                        RoleAssignCard(
                            role = role,
                            accent = accent,
                            assignment = assignment,
                            assignedMember = assignedMember,
                            isHighlighted = highlightedRoleLabel == role.label,
                            onCardBoundsChanged = { bounds -> if (role.isActive) cardBounds[role.label] = bounds else cardBounds.remove(role.label) },
                            onChipDragStart = { member, windowPos -> handleDragStart(member, role.label, windowPos) },
                            onChipDrag = { delta -> handleDrag(delta) },
                            onChipDragEnd = { handleDragEnd() },
                            onUnassign = { onAssign(role.label, null) },
                            onToggleActive = { onToggleRoleActive(role.id, !role.isActive) },
                            onUpdateTime = { s, e -> onUpdateTime(role.label, s, e) },
                            modifier = Modifier.widthIn(min = 240.dp, max = 280.dp)
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
                        .background(chipColor.copy(alpha = 0.9f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.DragIndicator, contentDescription = null, tint = chipTextColor.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    Text(draggedMember?.name ?: "", style = MaterialTheme.typography.labelMedium, color = chipTextColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun RoleAssignCard(
    role: RotationRoleUi,
    accent: Color,
    assignment: RoleAssignmentUi?,
    assignedMember: MemberUi?,
    isHighlighted: Boolean,
    onCardBoundsChanged: (Rect) -> Unit,
    onChipDragStart: (MemberUi, Offset) -> Unit,
    onChipDrag: (Offset) -> Unit,
    onChipDragEnd: () -> Unit,
    onUnassign: () -> Unit,
    onToggleActive: () -> Unit,
    onUpdateTime: (startMin: Int, endMin: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    val startMin = assignment?.startMin ?: role.startMin
    val endMin = assignment?.endMin ?: role.endMin

    Card(
        modifier = modifier
            .onGloballyPositioned { coords -> onCardBoundsChanged(coords.boundsInWindow()) }
            .then(
                if (isHighlighted) Modifier.border(2.5.dp, accent, RoundedCornerShape(20.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(if (isHighlighted) 10.dp else 3.dp)
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth()
                    .background(accent.copy(alpha = if (role.isActive) 0.14f else 0.06f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(if (role.isActive) accent else appColors.grey400))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        role.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                        color = if (role.isActive) accent else appColors.grey500,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = role.isActive, onCheckedChange = { onToggleActive() },
                        modifier = Modifier.size(width = 36.dp, height = 20.dp),
                        colors = SwitchDefaults.colors(checkedTrackColor = accent)
                    )
                }
            }

            Column(Modifier.padding(14.dp)) {
                if (!role.isActive) {
                    Text("사용 안 함", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RotationTimePickerField(
                            label = "출근", minutes = startMin, onChange = { onUpdateTime(it, endMin) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(6.dp))
                        RotationTimePickerField(
                            label = "퇴근", minutes = endMin, onChange = { onUpdateTime(startMin, it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    if (assignedMember == null) {
                        Box(
                            Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PersonOutline, contentDescription = null, tint = appColors.grey400, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("여기로 드래그", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DraggableMemberChip(
                                member = assignedMember,
                                onDragStart = { windowPos -> onChipDragStart(assignedMember, windowPos) },
                                onDrag = onChipDrag,
                                onDragEnd = onChipDragEnd,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onUnassign, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "배정 해제", tint = appColors.grey400, modifier = Modifier.size(14.dp))
                            }
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
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val memberColor = member.colorHex.toColor()
    var chipWindowBounds by remember { mutableStateOf(Rect.Zero) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(memberColor.copy(alpha = 0.35f))
            .onGloballyPositioned { coords -> chipWindowBounds = coords.boundsInWindow() }
            .pointerInput(member.id) {
                detectDragGestures(
                    onDragStart = { localOffset ->
                        onDragStart(Offset(chipWindowBounds.left + localOffset.x, chipWindowBounds.top + localOffset.y))
                    },
                    onDrag = { _, dragAmount -> onDrag(dragAmount) },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .padding(start = 10.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Default.DragIndicator, contentDescription = null, tint = chipTextColor.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        Box(
            Modifier.size(26.dp).clip(CircleShape).background(memberColor),
            contentAlignment = Alignment.Center
        ) {
            Text(member.name.take(1), style = MaterialTheme.typography.labelSmall, color = chipTextColor, fontWeight = FontWeight.Bold)
        }
        Text(member.name, style = MaterialTheme.typography.bodyMedium, color = chipTextColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyStateButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.PersonAdd
) {
    example.yf.fruit_hall.ui.component.util.ClickShrinkEffect(shrinkFactor = 0.96f, onClick = onClick) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}
