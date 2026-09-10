package example.yf.fruit_hall.ui.rotation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.R
import example.yf.fruit_hall.core.rotation.CellState
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.rotation.component.BreakEditDialog
import example.yf.fruit_hall.ui.rotation.component.PositionPickerDialog
import example.yf.fruit_hall.ui.rotation.component.PresetManageDialog
import example.yf.fruit_hall.ui.rotation.component.RotationDialogScaffold
import example.yf.fruit_hall.ui.rotation.component.RotationGridPanel
import example.yf.fruit_hall.ui.rotation.component.RotationMemberDialog
import example.yf.fruit_hall.ui.rotation.component.RotationSettingsDialog
import example.yf.fruit_hall.ui.rotation.component.RotationTimePickerField
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlinx.coroutines.delay

/** 사람별 고강도 부담. 하단 요약과 상세 다이얼로그가 같은 값을 보여주도록 한 곳에서 계산한다. */
private data class HighLoad(val memberName: String, val perPosition: List<Pair<String, Int>>) {
    val total: Int get() = perPosition.sumOf { it.second }
}

/**
 * 고강도 포지션을 **자리별로 나눠** 몇 번 들어갔는지 사람별로 센다(포스 2번 / 문지기 1번 …).
 * 합계만 보면 "A는 포스만 3번, B는 문지기만 3번"도 똑같아 보여서 편중이 안 드러난다 —
 * 기획서가 계층3(고강도 종류별 편차)을 따로 둔 이유와 같다(§6-1).
 * 0번인 자리도 빼지 않는다. 안 들어간 자리가 보여야 몰린 걸 알아챌 수 있다.
 */
private fun highLoadPerMember(
    rows: List<RotationRowUi>,
    positions: List<RotationPositionUi>
): List<HighLoad> {
    val highPositions = positions.filter { it.isHigh }.sortedBy { it.sortOrder }
    if (highPositions.isEmpty()) return emptyList()
    return rows
        .filter { it.memberId != null }
        .map { row ->
            HighLoad(
                memberName = row.name,
                perPosition = highPositions.map { position ->
                    position.name to row.cells.count { it.state == CellState.ASSIGNED && it.positionId == position.id }
                }
            )
        }
        .sortedByDescending { it.total }
}

@Composable
private fun RotationTopBarChip(text: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    ClickShrinkEffect(shrinkFactor = 0.95f, onClick = onClick) {
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RotationScreen(
    viewModel: RotationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val appColors = AppTheme.colors
    val presetListState = rememberLazyListState()
    var pendingResetAssignments by remember { mutableStateOf(false) }

    LaunchedEffect(state.selectedPresetId, state.presets) {
        val idx = state.presets.indexOfFirst { it.id == state.selectedPresetId }
        if (idx >= 0) presetListState.animateScrollToItem(idx)
    }

    // 안내 문구는 스스로 사라져야 한다 — 지우는 곳이 없어서 한 번 뜨면 계속 남아 있었다.
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            delay(3000)
            viewModel.dismissError()
        }
    }

    var presetManageTopTab by remember { mutableStateOf(0) }
    var showViolationDetail by remember { mutableStateOf(false) }
    var showTodayRoster by remember { mutableStateOf(false) }
    var positionPickerTarget by remember { mutableStateOf<Pair<Int, Long>?>(null) }
    var manualCursorMin by remember { mutableStateOf<Int?>(null) }

    // 하단 요약과 상세 다이얼로그가 같은 집계를 쓴다. 표 전체를 훑는 계산이라 행·포지션이
    // 바뀔 때만 다시 센다.
    val highLoad = remember(state.rows, state.positions) { highLoadPerMember(state.rows, state.positions) }

    // 안내 문구는 고정 문구(리소스)와 알고리즘 예외 원문 두 갈래로 온다 — 그리기 직전에 한 번만 푼다.
    val errorText = when (val message = state.errorMessage) {
        is RotationMessage.Res -> stringResource(message.id)
        is RotationMessage.Raw -> message.text
        null -> null
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 상단 바 (§10-1) — 기존 포지션 탭 PositionTopBar와 동일한 아이콘 버튼 스타일
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.presets.isEmpty()) {
                Text(
                    stringResource(R.string.rotation_preset_none),
                    style = MaterialTheme.typography.labelLarge, color = appColors.grey400,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            } else {
                LazyRow(
                    state = presetListState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.presets, key = { it.id }) { preset ->
                        val selected = preset.id == state.selectedPresetId
                        ClickShrinkEffect(shrinkFactor = 0.95f, onClick = { viewModel.selectPreset(preset.id) }) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) appColors.primary500 else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    preset.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selected) appColors.white else appColors.grey700
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // 스케줄 탭에 오늘 날짜 데이터가 있을 때만 띄운다 — 없으면 눌러봐야 빈 목록이다.
                if (state.todayShifts.isNotEmpty()) {
                    RotationTopBarChip(stringResource(R.string.rotation_top_today_schedule, state.todayShifts.size), Icons.Default.Today, appColors.success700) {
                        showTodayRoster = true
                    }
                }
                RotationTopBarChip(stringResource(R.string.rotation_top_member_manage), Icons.Default.Person, appColors.primary500) { viewModel.setDialog(showMemberManage = true) }
                RotationTopBarChip(stringResource(R.string.rotation_top_preset_manage), Icons.Default.Groups, appColors.warning500) {
                    presetManageTopTab = 0
                    viewModel.setDialog(showPresetManage = true)
                }
                RotationTopBarChip(stringResource(R.string.rotation_top_break_edit), Icons.Default.FreeBreakfast, appColors.crimson400) { viewModel.setDialog(showBreakEdit = true) }
                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 2.dp))
                RotationTopBarChip(stringResource(R.string.rotation_top_settings), Icons.Default.Tune, appColors.grey700) { viewModel.setDialog(showSettings = true) }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // 표는 하나다 (§10-2 세로축=사람 / 가로축=시간). 생성 전에는 근무시간만, 생성 후에는
        // 알고리즘이 채운 포지션 칩이 셀에 들어간다. 포지션은 사용자가 미리 고르지 않는다.
        val idleMembers = state.members.filter { m -> state.rows.none { it.memberId == m.id } }
        RotationGridPanel(
            slots = state.slots,
            rows = state.rows,
            isDraft = state.isDraft,
            selectedCell = state.selectedCell,
            idleMembers = idleMembers,
            scheduleTemplates = state.scheduleTemplates,
            positions = state.positions,
            selectedCursorMin = manualCursorMin,
            onRequestPositionPicker = { slotIndex, memberId -> positionPickerTarget = slotIndex to memberId },
            onTogglePin = viewModel::togglePin,
            onDropMemberOnRow = viewModel::dropMemberOnRow,
            onApplyTemplate = viewModel::applyTemplateToRow,
            onSetCellPosition = { slotIndex, memberId, positionId ->
                viewModel.setCellPosition(slotIndex, memberId, positionId, isBreak = positionId == null)
            },
            onTapHeaderSlot = { min -> manualCursorMin = min },
            onOpenMemberManage = { viewModel.setDialog(showMemberManage = true) },
            onAddRow = viewModel::addScheduleRow,
            onDeleteRow = viewModel::deleteScheduleRow,
            onToggleExclude = viewModel::toggleExcludeFromAssign,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
            Column {
                if (!state.isDraft || state.violations.isNotEmpty() || errorText != null || state.isGenerating) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!state.isDraft) {
                            val hasViolations = state.violations.isNotEmpty()
                            val badgeColor = if (hasViolations) appColors.crimson500 else appColors.grey500
                            ClickShrinkEffect(shrinkFactor = 0.96f, onClick = { showViolationDetail = true }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CircleNotifications, contentDescription = null, tint = badgeColor, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        if (hasViolations) stringResource(R.string.rotation_violation_count, state.violations.size)
                                        else stringResource(R.string.rotation_violation_none),
                                        color = badgeColor, style = MaterialTheme.typography.labelSmall
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(stringResource(R.string.rotation_violation_detail_link), color = badgeColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        errorText?.let {
                            Spacer(Modifier.width(12.dp))
                            Text(it, color = appColors.crimson500, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.weight(1f))
                        if (state.isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = appColors.primary500)
                        }
                    }
                }

                // 사람별 고강도 부담을 위반 줄 바로 아래에 펼쳐 놓는다 — 공평한지 아닌지가 이 탭의 핵심이라
                // 다이얼로그를 열어야만 보이면 늦다. 많이 맡은 사람이 앞에 오도록 총 횟수 내림차순이다.
                if (highLoad.any { it.total > 0 }) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val chipFormat = stringResource(R.string.rotation_high_load_chip)
                        Text(
                            stringResource(R.string.rotation_high_load_label), style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold, color = appColors.grey600
                        )
                        highLoad.forEach { load ->
                            Row(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(appColors.crimson50)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    load.memberName, style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    load.perPosition.joinToString(" · ") { (name, count) ->
                                        chipFormat.format(name, count)
                                    },
                                    style = MaterialTheme.typography.labelSmall, color = appColors.grey600
                                )
                            }
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ) {
                    // 근무조가 하나도 없으면(=표가 "근무 스케줄이 없습니다" 빈 상태) 지울 배정 자체가 없다 —
                    // 눌러도 아무 효과가 없는 버튼을 보여주면 헷갈리기만 하므로 숨긴다.
                    if (state.rows.isNotEmpty()) {
                        RotationActionButton(
                            text = stringResource(R.string.rotation_action_reset), icon = Icons.Default.RestartAlt,
                            bg = appColors.crimson50, fg = appColors.crimson500,
                            onClick = { pendingResetAssignments = true }
                        )
                    }
                    if (!state.isDraft) {
                        val cursorSet = manualCursorMin != null
                        RotationActionButton(
                            text = if (cursorSet) stringResource(R.string.rotation_action_regenerate_at, formatMinutes(manualCursorMin!!))
                            else stringResource(R.string.rotation_action_regenerate_pick),
                            icon = Icons.Default.CalendarViewDay,
                            bg = if (cursorSet) appColors.primary500 else MaterialTheme.colorScheme.surfaceVariant,
                            fg = if (cursorSet) appColors.white else appColors.grey700,
                            onClick = {
                                val cursor = manualCursorMin
                                if (cursor != null) {
                                    viewModel.regenerateFromCursor(cursor)
                                    manualCursorMin = null
                                } else {
                                    viewModel.setDialog(showCursorPicker = true)
                                }
                            }
                        )
                    }
                    RotationActionButton(
                        text = if (state.isDraft) stringResource(R.string.rotation_action_assign)
                        else stringResource(R.string.rotation_regenerate_full),
                        icon = Icons.Default.Refresh,
                        bg = appColors.primary500, fg = appColors.white,
                        onClick = viewModel::regenerateFull
                    )
                }
                Text(
                    if (state.isDraft) stringResource(R.string.rotation_footer_hint_draft)
                    else stringResource(R.string.rotation_footer_hint_generated),
                    style = MaterialTheme.typography.labelSmall, color = appColors.grey500,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }
        }
    }

    if (state.showMemberManage) {
        RotationMemberDialog(
            members = state.members,
            onAddMember = viewModel::addMember,
            onDeleteMember = viewModel::deleteMember,
            onDismiss = { viewModel.setDialog(showMemberManage = false) }
        )
    }
    if (state.showPresetManage) {
        PresetManageDialog(
            presets = state.presets,
            selectedPresetId = state.selectedPresetId,
            onSelectPreset = viewModel::selectPreset,
            onAddPreset = { name -> viewModel.savePreset(0, name, null) },
            onRenamePreset = viewModel::renamePreset,
            onSetDefault = viewModel::setDefaultPreset,
            onDeletePreset = viewModel::deletePreset,
            positions = state.positions,
            onSavePosition = viewModel::savePosition,
            onDeletePosition = viewModel::deletePosition,
            onTogglePositionActive = viewModel::setPositionActive,
            templates = state.scheduleTemplates,
            onSaveTemplate = viewModel::saveScheduleTemplate,
            onDeleteTemplate = viewModel::deleteScheduleTemplate,
            onDismiss = { viewModel.setDialog(showPresetManage = false) },
            initialTab = presetManageTopTab
        )
    }
    if (state.showBreakEdit) {
        val breakMemberIds = state.breaks.map { it.memberId }.toSet()
        val assignedMemberIds = state.roleAssignments.mapNotNull { it.memberId }.toSet()
        BreakEditDialog(
            breaks = state.breaks,
            assignableMembers = state.members.filter { it.id in assignedMemberIds && it.id !in breakMemberIds },
            defaultBreakStartMin = state.settings.defaultBreakStartMin,
            onGenerateDefault = viewModel::generateDefaultBreaks,
            onUpdate = viewModel::updateBreak,
            onRemove = viewModel::removeBreak,
            onDismiss = { viewModel.setDialog(showBreakEdit = false) }
        )
    }
    if (state.showSettings) {
        RotationSettingsDialog(
            settings = state.settings,
            onSave = viewModel::updateSettings,
            onDismiss = { viewModel.setDialog(showSettings = false) }
        )
    }

    if (showTodayRoster) {
        // 원본은 스케줄 탭이다(포지션2의 인원배정과 별개). 근무조는 오늘 아직 안 꽂았어도
        // 스케줄에 올라와 있으면 누가 나오는지 먼저 확인할 수 있어야 한다.
        val byShift = state.todayShifts.groupBy { it.shift }
        RotationDialogScaffold(
            title = stringResource(R.string.rotation_today_roster_title, state.date),
            icon = Icons.Default.Today,
            headerColor = appColors.success700,
            onDismiss = { showTodayRoster = false },
            widthFraction = 0.4f,
            footer = { TextButton(onClick = { showTodayRoster = false }) { Text(stringResource(R.string.rotation_close), color = appColors.primary700) } }
        ) {
            Text(
                stringResource(R.string.rotation_today_roster_desc),
                style = MaterialTheme.typography.bodySmall, color = appColors.grey500
            )
            Spacer(Modifier.height(10.dp))
            byShift.forEach { (shift, people) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                    Text(
                        shift, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                        color = appColors.success700, modifier = Modifier.width(64.dp)
                    )
                    Text(
                        people.joinToString(", ") { it.personName },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // 생성은 몇 초 걸릴 수 있다. 하단의 작은 스피너만으로는 눌렸는지조차 알기 어려워서
    // 화면 가운데에 진행 표시를 띄우고, 그동안 다른 조작이 섞이지 않게 막는다.
    if (state.isGenerating) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    Modifier.padding(horizontal = 32.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(38.dp), strokeWidth = 3.dp, color = appColors.primary500)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.rotation_generating_title), style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.rotation_generating_desc),
                        style = MaterialTheme.typography.labelSmall, color = appColors.grey500
                    )
                }
            }
        }
    }

    if (showViolationDetail) {
        val slotById = state.slots.associateBy { it.index }
        val nameById = state.members.associateBy { it.id }
        RotationDialogScaffold(
            title = stringResource(R.string.rotation_violation_dialog_title),
            icon = Icons.Default.CircleNotifications,
            headerColor = appColors.crimson500,
            onDismiss = { showViolationDetail = false },
            widthFraction = 0.46f,
            footer = { TextButton(onClick = { showViolationDetail = false }) { Text(stringResource(R.string.rotation_close), color = appColors.primary700) } }
        ) {
            Text(stringResource(R.string.rotation_high_load_section), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            if (highLoad.isEmpty()) {
                Text(stringResource(R.string.rotation_high_load_empty), style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
            } else {
                val countFormat = stringResource(R.string.rotation_high_load_count)
                highLoad.forEach { load ->
                    Text(
                        stringResource(
                            R.string.rotation_high_load_entry,
                            load.memberName,
                            load.perPosition.joinToString(" · ") { (name, count) -> countFormat.format(name, count) }
                        ),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(14.dp))

            Text(stringResource(R.string.rotation_violation_section), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            if (state.violations.isEmpty()) {
                Text(stringResource(R.string.rotation_violation_empty), style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
            } else {
                Text(
                    stringResource(R.string.rotation_violation_desc),
                    style = MaterialTheme.typography.bodySmall, color = appColors.grey500
                )
                Spacer(Modifier.height(6.dp))
                state.violations.forEach { v ->
                    val slot = slotById[v.slotIndex]
                    val memberName = nameById[v.memberId]?.name ?: "?"
                    val timeText = slot?.let { "${formatMinutes(it.startMin)}~${formatMinutes(it.endMin)}" } ?: ""
                    ClickShrinkEffect(
                        shrinkFactor = 0.97f,
                        onClick = {
                            positionPickerTarget = v.slotIndex to v.memberId
                            showViolationDetail = false
                        }
                    ) {
                        Text(
                            stringResource(R.string.rotation_violation_entry, memberName, timeText, v.message),
                            style = MaterialTheme.typography.labelMedium, color = appColors.primary700,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }

    positionPickerTarget?.let { (slotIndex, memberId) ->
        PositionPickerDialog(
            positions = state.positions,
            onPickPosition = { position ->
                viewModel.setCellPosition(slotIndex, memberId, position.id, isBreak = false)
                positionPickerTarget = null
            },
            onPickBreak = {
                viewModel.setCellPosition(slotIndex, memberId, null, isBreak = true)
                positionPickerTarget = null
            },
            onDismiss = { positionPickerTarget = null }
        )
    }

    if (state.showCursorPicker) {
        val nowMin = remember(state.showCursorPicker) {
            java.time.LocalTime.now().let { it.hour * 60 + it.minute }
        }
        var cursorMinutes by remember(state.showCursorPicker) { mutableStateOf(nowMin) }
        RotationDialogScaffold(
            title = stringResource(R.string.rotation_cursor_picker_title),
            icon = Icons.Default.CalendarViewDay,
            headerColor = appColors.primary500,
            onDismiss = { viewModel.setDialog(showCursorPicker = false) },
            widthFraction = 0.38f,
            maxHeight = 320.dp,
            footer = {
                TextButton(onClick = { viewModel.setDialog(showCursorPicker = false) }) { Text(stringResource(R.string.rotation_cancel), color = appColors.grey600) }
                TextButton(onClick = {
                    viewModel.setDialog(showCursorPicker = false)
                    viewModel.regenerateFromCursor(cursorMinutes)
                }) {
                    Text(stringResource(R.string.rotation_cursor_picker_confirm), color = appColors.primary700)
                }
            }
        ) {
            Text(
                stringResource(R.string.rotation_cursor_picker_desc),
                style = MaterialTheme.typography.labelSmall, color = appColors.grey500
            )
            Spacer(Modifier.height(12.dp))
            RotationTimePickerField(
                label = stringResource(R.string.rotation_time_label), minutes = cursorMinutes, onChange = { cursorMinutes = it },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (pendingResetAssignments) {
        RotationDialogScaffold(
            title = stringResource(R.string.rotation_action_reset),
            icon = Icons.Default.RestartAlt,
            headerColor = appColors.crimson500,
            onDismiss = { pendingResetAssignments = false },
            widthFraction = 0.44f,
            maxHeight = 460.dp,
            footer = {
                TextButton(onClick = { pendingResetAssignments = false }) { Text(stringResource(R.string.rotation_cancel), color = appColors.grey600) }
            }
        ) {
            Text(
                stringResource(R.string.rotation_reset_desc),
                style = MaterialTheme.typography.bodySmall, color = appColors.grey500
            )
            Spacer(Modifier.height(12.dp))
            ResetOptionRow(
                title = stringResource(R.string.rotation_reset_positions_title),
                description = stringResource(R.string.rotation_reset_positions_desc),
                onClick = { viewModel.resetAssignments(RotationResetTarget.POSITIONS); pendingResetAssignments = false }
            )
            ResetOptionRow(
                title = stringResource(R.string.rotation_reset_members_title),
                description = stringResource(R.string.rotation_reset_members_desc),
                onClick = { viewModel.resetAssignments(RotationResetTarget.MEMBERS); pendingResetAssignments = false }
            )
            ResetOptionRow(
                title = stringResource(R.string.rotation_reset_schedules_title),
                description = stringResource(R.string.rotation_reset_schedules_desc),
                onClick = { viewModel.resetAssignments(RotationResetTarget.SCHEDULES); pendingResetAssignments = false }
            )
            ResetOptionRow(
                title = stringResource(R.string.rotation_reset_all_title),
                description = stringResource(R.string.rotation_reset_all_desc),
                isDestructive = true,
                onClick = { viewModel.resetAssignments(RotationResetTarget.ALL); pendingResetAssignments = false }
            )
        }
    }
}

/** 배정 초기화 다이얼로그의 선택지 한 줄. 무엇이 남는지까지 적어야 눌러도 되는지 판단할 수 있다. */
@Composable
private fun ResetOptionRow(
    title: String,
    description: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val appColors = AppTheme.colors
    val accent = if (isDestructive) appColors.crimson500 else appColors.primary700
    ClickShrinkEffect(shrinkFactor = 0.97f, onClick = onClick) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.08f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = accent)
            Spacer(Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.labelSmall, color = appColors.grey600)
        }
    }
}

@Composable
private fun RotationActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    ClickShrinkEffect(shrinkFactor = 0.93f, onClick = onClick) {
        Row(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = fg)
        }
    }
}
