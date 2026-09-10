package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import example.yf.fruit_hall.R
import example.yf.fruit_hall.core.rotation.CellState
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.rotation.MemberUi
import example.yf.fruit_hall.ui.rotation.RotationCellUi
import example.yf.fruit_hall.ui.rotation.RotationPositionUi
import example.yf.fruit_hall.ui.rotation.RotationRowUi
import example.yf.fruit_hall.ui.rotation.RotationScheduleTemplateUi
import example.yf.fruit_hall.ui.rotation.RotationSlotUi
import example.yf.fruit_hall.ui.rotation.SelectedCell
import example.yf.fruit_hall.ui.rotation.formatMinutes
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlin.math.roundToInt

private val CELL_WIDTH = 84.dp
// 이름 칸에는 아바타(28) + 이름·스케줄명 + 제외/삭제 아이콘(48) + 좌우 패딩(16)이 함께 들어간다.
// 132dp였을 때 글자에 40dp밖에 안 남아 스케줄명이 잘렸다 — 글자 몫으로 100dp 이상을 확보한다.
private val NAME_WIDTH = 196.dp
private val ROW_HEIGHT = 62.dp

/**
 * 하루 근무표 (§10-2 "세로축 = 사람 / 가로축 = 시간"). 표는 하나뿐이고 생성 전후로 셀 내용만 달라진다.
 *  - 생성 전(isDraft): 근무 중인 칸만 옅게 칠하고 브레이크는 휴게 칩. 포지션은 비어 있다.
 *  - 생성 후: 알고리즘이 채운 포지션 칩 + 핀 고정.
 * 이름 칸에 멤버를 드래그해 담당자를 넣고 바꾸며, 칸 위에 근무스케줄·포지션을 떨어뜨려 수정한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RotationGridPanel(
    slots: List<RotationSlotUi>,
    rows: List<RotationRowUi>,
    isDraft: Boolean,
    selectedCell: SelectedCell?,
    idleMembers: List<MemberUi>,
    scheduleTemplates: List<RotationScheduleTemplateUi>,
    positions: List<RotationPositionUi>,
    selectedCursorMin: Int?,
    onRequestPositionPicker: (slotIndex: Int, memberId: Long) -> Unit,
    onTogglePin: (slotIndex: Int, memberId: Long) -> Unit,
    onDropMemberOnRow: (targetRoleLabel: String, sourceRoleLabel: String?, memberId: Long) -> Unit,
    onApplyTemplate: (roleLabel: String, template: RotationScheduleTemplateUi) -> Unit,
    /** positionId가 null이면 그 칸을 휴게로 바꾼다. */
    onSetCellPosition: (slotIndex: Int, memberId: Long, positionId: Long?) -> Unit,
    onTapHeaderSlot: (startMin: Int) -> Unit,
    onOpenMemberManage: () -> Unit,
    onAddRow: () -> Unit,
    onDeleteRow: (roleLabel: String) -> Unit,
    /** 표에는 두되 포지션 배정에서만 빼기. */
    onToggleExclude: (roleLabel: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    val gridLine = MaterialTheme.colorScheme.outlineVariant

    val rowBounds = remember { mutableStateMapOf<String, Rect>() }
    val cellBounds = remember { mutableStateMapOf<Pair<String, Int>, Rect>() }
    var overallBounds by remember { mutableStateOf(Rect.Zero) }

    // 줄을 지우거나 순서가 바뀌어도 예전 좌표가 맵에 그대로 남는다. 그러면 그 유령 영역에 떨어뜨렸을 때
    // 없는 줄을 target으로 잡아 드롭이 조용히 씹히거나 엉뚱한 줄에 적용된다 — 현재 줄만 남긴다.
    val currentLabels = rows.map { it.roleLabel }.toSet()
    LaunchedEffect(currentLabels) {
        rowBounds.keys.retainAll(currentLabels)
        cellBounds.keys.retainAll { it.first in currentLabels }
    }

    var draggedMember by remember { mutableStateOf<MemberUi?>(null) }
    var dragSourceRoleLabel by remember { mutableStateOf<String?>(null) }
    var memberDragPos by remember { mutableStateOf(Offset.Zero) }
    var highlightedRoleLabel by remember { mutableStateOf<String?>(null) }

    var draggedPaletteItem by remember { mutableStateOf<PaletteItem?>(null) }
    var palettePos by remember { mutableStateOf(Offset.Zero) }
    var highlightedCellKey by remember { mutableStateOf<Pair<String, Int>?>(null) }

    fun handleMemberDragStart(member: MemberUi, sourceRoleLabel: String?, windowPos: Offset) {
        draggedMember = member
        dragSourceRoleLabel = sourceRoleLabel
        memberDragPos = windowPos
    }

    fun handleMemberDrag(delta: Offset) {
        memberDragPos += delta
        highlightedRoleLabel = rowBounds.entries
            .firstOrNull { (_, rect) -> rect.contains(memberDragPos) }
            ?.key
            ?.takeIf { it != dragSourceRoleLabel }
    }

    fun handleMemberDragEnd() {
        val target = highlightedRoleLabel
        val member = draggedMember
        if (target != null && member != null) onDropMemberOnRow(target, dragSourceRoleLabel, member.id)
        draggedMember = null
        dragSourceRoleLabel = null
        highlightedRoleLabel = null
        memberDragPos = Offset.Zero
    }

    fun handlePaletteDragStart(item: PaletteItem, windowPos: Offset) {
        draggedPaletteItem = item
        palettePos = windowPos
    }

    fun handlePaletteDrag(delta: Offset) {
        palettePos += delta
        highlightedCellKey = cellBounds.entries.firstOrNull { (_, rect) -> rect.contains(palettePos) }?.key
            ?: rowBounds.entries.firstOrNull { (_, rect) -> rect.contains(palettePos) }?.key?.let { it to -1 }
    }

    fun handlePaletteDragEnd() {
        val key = highlightedCellKey
        val item = draggedPaletteItem
        if (key != null && item != null) {
            val (roleLabel, slotIndex) = key
            val row = rows.firstOrNull { it.roleLabel == roleLabel }
            val memberId = row?.memberId
            when (item) {
                // 근무 스케줄은 그 행의 출퇴근 시각 + 기본 브레이크를 통째로 바꾼다(이름 칸·시간 칸 어디든).
                is PaletteItem.Template -> onApplyTemplate(roleLabel, item.template)
                // 포지션·휴게는 특정 시간 칸에 떨어뜨려야 한다 — 그 칸을 강제로 대체한다.
                is PaletteItem.PositionItem -> if (slotIndex != -1 && memberId != null) {
                    onSetCellPosition(slotIndex, memberId, item.position.id)
                }
                PaletteItem.BreakItem -> if (slotIndex != -1 && memberId != null) {
                    onSetCellPosition(slotIndex, memberId, null)
                }
            }
        }
        draggedPaletteItem = null
        highlightedCellKey = null
        palettePos = Offset.Zero
    }

    Box(modifier.onGloballyPositioned { overallBounds = it.boundsInWindow() }) {
        Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            // 근무조가 없어도 표(시간축)는 항상 그린다. 전체 화면을 빈 상태 안내로 덮으면 표가 나와야 할
            // 자리를 가려서 아무것도 못 하는 막다른 화면이 된다 — 안내는 표 아래 한 줄로만 둔다.
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
                    Column(Modifier.horizontalScroll(rememberScrollState())) {
                        // 헤더 — 가로축은 시간
                        Row {
                            Box(
                                Modifier.width(NAME_WIDTH).height(ROW_HEIGHT)
                                    .background(MaterialTheme.colorScheme.surfaceVariant).border(0.5.dp, gridLine),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.rotation_grid_name_header),
                                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            slots.forEach { slot ->
                                val isCursorSelected = !isDraft && selectedCursorMin == slot.startMin
                                Box(
                                    Modifier.width(CELL_WIDTH).height(ROW_HEIGHT)
                                        .background(if (slot.isFrozen) appColors.grey200 else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(0.5.dp, gridLine)
                                        .then(if (isCursorSelected) Modifier.border(2.dp, appColors.primary700) else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDraft) {
                                        HeaderTimeLabel(slot, appColors)
                                    } else {
                                        ClickShrinkEffect(shrinkFactor = 0.94f, onClick = { onTapHeaderSlot(slot.startMin) }) {
                                            HeaderTimeLabel(slot, appColors)
                                        }
                                    }
                                }
                            }
                        }

                        // 행 하나 = 사람 하나
                        rows.forEachIndexed { rowIndex, row ->
                            val rowTint = if (rowIndex % 2 == 0) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            val isRowHighlighted = highlightedRoleLabel == row.roleLabel ||
                                highlightedCellKey?.first == row.roleLabel

                            Row(
                                Modifier.height(ROW_HEIGHT)
                                    .onGloballyPositioned { coords -> rowBounds[row.roleLabel] = coords.boundsInWindow() }
                            ) {
                                RowNameCell(
                                    row = row,
                                    rowTint = rowTint,
                                    gridLine = gridLine,
                                    isHighlighted = isRowHighlighted,
                                    onDragStart = { member, pos -> handleMemberDragStart(member, row.roleLabel, pos) },
                                    onDrag = { delta -> handleMemberDrag(delta) },
                                    onDragEnd = { handleMemberDragEnd() },
                                    onDelete = { onDeleteRow(row.roleLabel) },
                                    onToggleExclude = { onToggleExclude(row.roleLabel) }
                                )
                                row.cells.forEachIndexed { cellIndex, cell ->
                                    // 한 사람의 브레이크가 여러 칸에 걸칠 수 있다(다른 사람 브레이크 시각이
                                    // 앵커라 그리드가 쪼개지는 경우). 앞뒤가 휴게인지 알려줘서 한 덩어리로 그린다.
                                    val prevIsBreak = row.cells.getOrNull(cellIndex - 1)?.state == CellState.BREAK
                                    val nextIsBreak = row.cells.getOrNull(cellIndex + 1)?.state == CellState.BREAK
                                    Box(
                                        Modifier
                                            .onGloballyPositioned { coords -> cellBounds[row.roleLabel to cell.slotIndex] = coords.boundsInWindow() }
                                            .then(
                                                if (highlightedCellKey == (row.roleLabel to cell.slotIndex)) Modifier.border(2.dp, appColors.crimson400)
                                                else Modifier
                                            )
                                    ) {
                                        RotationCell(
                                            cell = cell,
                                            prevIsBreak = prevIsBreak,
                                            nextIsBreak = nextIsBreak,
                                            memberColor = row.colorHex.toRotationColorOrNull() ?: appColors.grey400,
                                            rowTint = rowTint,
                                            gridLine = gridLine,
                                            isSelected = selectedCell?.slotIndex == cell.slotIndex && selectedCell.memberId == row.memberId,
                                            onTap = { row.memberId?.let { onRequestPositionPicker(cell.slotIndex, it) } },
                                            onTogglePin = { row.memberId?.let { onTogglePin(cell.slotIndex, it) } }
                                        )
                                    }
                                }
                            }
                        }

                        // 줄 추가는 표 안에서 끝낸다 — 별도 관리 화면으로 보내면 거기서 추가해도
                        // 이 표가 안 바뀐 것처럼 보여 막다른 길이 된다.
                        Row(Modifier.height(ROW_HEIGHT)) {
                            Box(
                                Modifier.width(NAME_WIDTH).fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.background).border(0.5.dp, gridLine),
                                contentAlignment = Alignment.Center
                            ) {
                                ClickShrinkEffect(shrinkFactor = 0.94f, onClick = onAddRow) {
                                    Row(
                                        Modifier.clip(RoundedCornerShape(50))
                                            .background(appColors.primary500.copy(alpha = 0.12f))
                                            .padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = appColors.primary500, modifier = Modifier.size(14.dp))
                                        Text(
                                            stringResource(R.string.rotation_grid_add_row),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold, color = appColors.primary500
                                        )
                                    }
                                }
                            }
                            slots.forEach { _ ->
                                Box(
                                    Modifier.width(CELL_WIDTH).height(ROW_HEIGHT)
                                        .background(MaterialTheme.colorScheme.background).border(0.5.dp, gridLine)
                                )
                            }
                        }
                    }
                }
        }

        // 쉬는 멤버 + 근무스케줄/포지션 팔레트 — 표보다 넓은 배경을 차지하지 않도록 최대한 좁게.
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.rotation_offduty_title),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenMemberManage, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.rotation_member_manage_edit), tint = appColors.primary500, modifier = Modifier.size(14.dp))
                }
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (idleMembers.isEmpty()) {
                    Text(stringResource(R.string.rotation_offduty_empty), style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                } else {
                    idleMembers.forEach { member ->
                        DraggableMemberChip(
                            member = member,
                            onDragStart = { windowPos -> handleMemberDragStart(member, null, windowPos) },
                            onDrag = { delta -> handleMemberDrag(delta) },
                            onDragEnd = { handleMemberDragEnd() }
                        )
                    }
                }
            }
            RotationSchedulePalette(
                templates = scheduleTemplates,
                positions = positions,
                onDragStart = { item, windowPos -> handlePaletteDragStart(item, windowPos) },
                onDrag = { delta -> handlePaletteDrag(delta) },
                onDragEnd = { handlePaletteDragEnd() }
            )
        }
        }

        // 드래그 중인 칩 미리보기 — 표·팔레트 전체보다 항상 위에 그려야 드래그하는 동안 가려지지 않는다.
        if (draggedMember != null) {
            DraggingChip(draggedMember?.name ?: "", draggedMember?.colorHex.toRotationColorOrNull() ?: appColors.grey400, memberDragPos, overallBounds)
        }
        if (draggedPaletteItem != null) {
            val item = draggedPaletteItem
            val label = when (item) {
                is PaletteItem.Template -> item.template.label
                is PaletteItem.PositionItem -> item.position.name
                PaletteItem.BreakItem -> stringResource(R.string.rotation_grid_break_label)
                null -> ""
            }
            val color = when (item) {
                is PaletteItem.PositionItem -> item.position.colorHex.toRotationColorOrNull() ?: appColors.success500
                PaletteItem.BreakItem -> appColors.grey400
                else -> appColors.primary200
            }
            DraggingChip(label, color, palettePos, overallBounds)
        }
    }
}

@Composable
private fun HeaderTimeLabel(slot: RotationSlotUi, appColors: example.yf.fruit_hall.ui.theme.AppColor) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            formatMinutes(slot.startMin), style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            formatMinutes(slot.endMin), style = MaterialTheme.typography.labelSmall,
            color = appColors.grey600
        )
    }
}

/** 이름 칸 — 이 표의 주인은 사람이다. 비어 있으면 여기로 멤버를 끌어다 놓는다. */
@Composable
private fun RowNameCell(
    row: RotationRowUi,
    rowTint: Color,
    gridLine: Color,
    isHighlighted: Boolean,
    onDragStart: (MemberUi, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDelete: () -> Unit,
    onToggleExclude: () -> Unit
) {
    val appColors = AppTheme.colors
    val memberColor = row.colorHex.toRotationColorOrNull() ?: appColors.grey400
    var bounds by remember { mutableStateOf(Rect.Zero) }

    Box(
        Modifier.width(NAME_WIDTH).fillMaxHeight()
            .background(rowTint)
            .border(0.5.dp, gridLine)
            .then(if (isHighlighted) Modifier.border(2.dp, appColors.primary700) else Modifier)
            .onGloballyPositioned { coords -> bounds = coords.boundsInWindow() }
            .then(
                if (row.memberId == null) Modifier else Modifier.pointerInput(row.memberId) {
                    detectDragGestures(
                        onDragStart = { local ->
                            onDragStart(
                                MemberUi(row.memberId, row.name, row.colorHex),
                                Offset(bounds.left + local.x, bounds.top + local.y)
                            )
                        },
                        onDrag = { _, delta -> onDrag(delta) },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    )
                }
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (row.memberId != null) {
                Box(
                    Modifier.size(22.dp).clip(CircleShape).background(memberColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        row.name.take(1), style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = rotationChipTextColor
                    )
                }
                Spacer(Modifier.width(6.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    if (row.memberId != null) row.name else stringResource(R.string.rotation_member_unassigned),
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                    color = when {
                        row.memberId == null -> appColors.grey400
                        row.isExcluded -> appColors.grey500
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (row.isExcluded) TextDecoration.LineThrough else null,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 넣어둔 근무 스케줄 이름(오픈·미들·마감 등)을 보여준다 — 포지션이 아니다. 포지션은
                    // 오른쪽 시간 칸에 알고리즘이 채운 칩으로만 나타나고 슬롯마다 계속 바뀐다.
                    val schedule = row.scheduleLabel
                    Icon(
                        Icons.Default.Schedule, contentDescription = null,
                        tint = if (schedule != null) appColors.grey700 else appColors.grey400,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        schedule ?: stringResource(R.string.rotation_grid_schedule_empty),
                        style = MaterialTheme.typography.labelSmall,
                        // 들어 있으면 진하게, 비어 있으면 흐리게 — 아직 채워야 할 줄이 한눈에 보여야 한다.
                        color = if (schedule != null) MaterialTheme.colorScheme.onSurface else appColors.grey400,
                        fontWeight = if (schedule != null) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // 사람은 표에 그대로 두고 포지션 배정에서만 빼는 토글 — 교육·행사 지원처럼 근무는 하지만
            // 자리를 맡지 않는 경우를 위해 둔다. 켜지면 이름에 취소선이 그어진다.
            if (row.memberId != null) {
                ClickShrinkEffect(shrinkFactor = 0.9f, onClick = onToggleExclude) {
                    Icon(
                        if (row.isExcluded) Icons.Default.Block else Icons.Default.HowToReg,
                        contentDescription = stringResource(R.string.rotation_grid_exclude_cd),
                        tint = if (row.isExcluded) appColors.crimson500 else appColors.grey400,
                        modifier = Modifier.size(24.dp).padding(5.dp)
                    )
                }
            }
            ClickShrinkEffect(shrinkFactor = 0.9f, onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.rotation_grid_delete_row),
                    tint = appColors.grey400, modifier = Modifier.size(24.dp).padding(6.dp)
                )
            }
        }
    }
}

@Composable
private fun DraggingChip(label: String, color: Color, windowPos: Offset, overallBounds: Rect) {
    Row(
        modifier = Modifier
            .offset {
                IntOffset(
                    (windowPos.x - overallBounds.left).roundToInt() - 60,
                    (windowPos.y - overallBounds.top).roundToInt() - 18
                )
            }
            .zIndex(100f)
            .shadow(12.dp, RoundedCornerShape(50))
            .background(color.copy(alpha = 0.9f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Default.DragIndicator, contentDescription = null, tint = rotationChipTextColor.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = rotationChipTextColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RotationCell(
    cell: RotationCellUi,
    prevIsBreak: Boolean,
    nextIsBreak: Boolean,
    memberColor: Color,
    rowTint: Color,
    gridLine: Color,
    isSelected: Boolean,
    onTap: () -> Unit,
    onTogglePin: () -> Unit
) {
    val appColors = AppTheme.colors
    val chipColor = cell.colorHex.toRotationColorOrNull() ?: memberColor
    val highlightBorder = when {
        isSelected -> appColors.primary700
        cell.hasViolation -> appColors.crimson500
        else -> null
    }

    Box(
        Modifier
            .width(CELL_WIDTH).height(ROW_HEIGHT)
            .background(rowTint)
            .border(0.5.dp, gridLine)
            .then(if (highlightBorder != null) Modifier.border(2.dp, highlightBorder) else Modifier)
    ) {
        when (cell.state) {
            CellState.ASSIGNED -> {
                // 생성 전이든 후든 누를 수 있다 — 알고리즘을 먼저 돌려야만 손댈 수 있게 막지 않는다.
                val positionName = cell.positionName
                ClickShrinkEffect(modifier = Modifier.fillMaxSize(), onClick = onTap) {
                    Box(Modifier.fillMaxSize().padding(5.dp), contentAlignment = Alignment.Center) {
                        if (positionName == null) {
                            // 근무 중이지만 아직 포지션이 없는 칸. 사람마다 다른 멤버 색을 옅게 깔면
                            // 포지션이 들어간 것처럼 보여서, 지정 안 된 칸은 전부 같은 연한 회색으로 둔다.
                            Box(Modifier.fillMaxSize().padding(1.dp).clip(RoundedCornerShape(8.dp)).background(appColors.grey200))
                        } else {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    // 등록한 포지션 색을 연하게 깔아 글씨(진회색)가 묻히지 않게 한다.
                                    .background(chipColor.copy(alpha = 0.45f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    positionName, style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold, color = rotationChipTextColor,
                                    textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                // 핀은 포지션이 들어 있는 칸에만 의미가 있다.
                if (positionName != null) {
                    ClickShrinkEffect(
                        modifier = Modifier.align(Alignment.TopEnd).size(18.dp),
                        onClick = onTogglePin
                    ) {
                        Icon(
                            if (cell.isPinned) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = stringResource(R.string.rotation_grid_pin_cd),
                            modifier = Modifier.size(13.dp),
                            tint = if (cell.isPinned) appColors.primary700 else appColors.grey500.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            CellState.BREAK -> {
                // 브레이크 하나가 여러 칸에 걸쳐도 "두 번 쉬는" 것처럼 보이면 안 된다 —
                // 이어진 칸끼리는 모서리를 펴서 한 덩어리로 잇고, 글자와 자물쇠는 첫 칸에만 둔다.
                val corner = 50.dp
                val shape = RoundedCornerShape(
                    topStart = if (prevIsBreak) 0.dp else corner,
                    bottomStart = if (prevIsBreak) 0.dp else corner,
                    topEnd = if (nextIsBreak) 0.dp else corner,
                    bottomEnd = if (nextIsBreak) 0.dp else corner
                )
                ClickShrinkEffect(modifier = Modifier.fillMaxSize(), onClick = onTap) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .clip(shape)
                                .background(appColors.grey300),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!prevIsBreak) {
                                Text(
                                    stringResource(R.string.rotation_grid_break_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium, color = appColors.grey800,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
                if (!prevIsBreak) {
                    ClickShrinkEffect(
                        modifier = Modifier.align(Alignment.TopEnd).size(18.dp),
                        onClick = onTogglePin
                    ) {
                        Icon(
                            if (cell.isPinned) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = stringResource(R.string.rotation_grid_pin_cd),
                            modifier = Modifier.size(13.dp),
                            tint = if (cell.isPinned) appColors.primary700 else appColors.grey500.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            CellState.OFF -> Unit
        }
    }
}
