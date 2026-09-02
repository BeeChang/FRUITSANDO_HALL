package example.yf.fruit_hall.ui.rotation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.rotation.component.BreakEditDialog
import example.yf.fruit_hall.ui.rotation.component.MemberAssignPanel
import example.yf.fruit_hall.ui.rotation.component.PositionManageDialog
import example.yf.fruit_hall.ui.rotation.component.PresetManageDialog
import example.yf.fruit_hall.ui.rotation.component.RotationGridPanel
import example.yf.fruit_hall.ui.rotation.component.RotationMemberDialog
import example.yf.fruit_hall.ui.rotation.component.RotationSettingsDialog
import example.yf.fruit_hall.ui.rotation.component.ScheduleManageDialog
import example.yf.fruit_hall.ui.theme.AppTheme

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

@Composable
fun RotationScreen(
    viewModel: RotationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val appColors = AppTheme.colors
    val presetListState = rememberLazyListState()

    LaunchedEffect(state.selectedPresetId, state.presets) {
        val idx = state.presets.indexOfFirst { it.id == state.selectedPresetId }
        if (idx >= 0) presetListState.animateScrollToItem(idx)
    }

    val showAssignUi = state.rows.isEmpty() || state.showMemberAssign

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
                    "프리셋 없음", style = MaterialTheme.typography.labelLarge, color = appColors.grey400,
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
                RotationTopBarChip("멤버 관리", Icons.Default.Person, appColors.primary500) { viewModel.setDialog(showMemberManage = true) }
                RotationTopBarChip("포지션 관리", Icons.AutoMirrored.Filled.List, appColors.secondary500) { viewModel.setDialog(showPositionManage = true) }
                RotationTopBarChip("스케줄 관리", Icons.Default.Schedule, appColors.success500) { viewModel.setDialog(showScheduleManage = true) }
                RotationTopBarChip("프리셋 관리", Icons.Default.Groups, appColors.warning500) { viewModel.setDialog(showPresetManage = true) }
                RotationTopBarChip("브레이크 편집", Icons.Default.FreeBreakfast, appColors.crimson400) { viewModel.setDialog(showBreakEdit = true) }
                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 2.dp))
                RotationTopBarChip("설정", Icons.Default.Tune, appColors.grey700) { viewModel.setDialog(showSettings = true) }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // 결과가 없으면 인원배정 화면을, 있으면 결과 그리드를 큰 화면에 보여준다
        if (showAssignUi) {
            MemberAssignPanel(
                roles = state.roles,
                positions = state.positions,
                assignments = state.roleAssignments,
                members = state.members,
                onAssign = viewModel::assignMember,
                onUpdateTime = viewModel::updateRoleTime,
                onToggleRoleActive = viewModel::setRoleActive,
                onOpenMemberManage = { viewModel.setDialog(showMemberManage = true) },
                onOpenPresetManage = { viewModel.setDialog(showPresetManage = true) },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        } else {
            RotationGridPanel(
                slots = state.slots,
                rows = state.rows,
                selectedCell = state.selectedCell,
                onTapCell = viewModel::tapCell,
                onTogglePin = viewModel::togglePin,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
            Column {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    state.score?.let {
                        Text("점수 $it", style = MaterialTheme.typography.labelMedium, color = appColors.grey700)
                        Spacer(Modifier.width(12.dp))
                    }
                    if (state.violations.isNotEmpty()) {
                        Icon(Icons.Default.CircleNotifications, contentDescription = null, tint = appColors.crimson500, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("위반 ${state.violations.size}건", color = appColors.crimson500, style = MaterialTheme.typography.labelMedium)
                    }
                    state.errorMessage?.let {
                        Spacer(Modifier.width(12.dp))
                        Text(it, color = appColors.crimson500, style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.weight(1f))
                    if (state.isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = appColors.primary500)
                        Spacer(Modifier.width(12.dp))
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    if (state.rows.isNotEmpty()) {
                        RotationActionButton(
                            text = if (state.showMemberAssign) "결과 보기" else "인원 재배정",
                            icon = Icons.Default.Person,
                            bg = MaterialTheme.colorScheme.surfaceVariant, fg = appColors.grey700,
                            onClick = { viewModel.setDialog(showMemberAssign = !state.showMemberAssign) }
                        )
                        RotationActionButton(
                            text = "여기부터 재생성", icon = Icons.Default.CalendarViewDay,
                            bg = MaterialTheme.colorScheme.surfaceVariant, fg = appColors.grey700,
                            onClick = viewModel::regenerateFromCursor
                        )
                    }
                    RotationActionButton(
                        text = "전체 재생성", icon = Icons.Default.Refresh,
                        bg = appColors.primary500, fg = appColors.white,
                        onClick = viewModel::regenerateFull
                    )
                }
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
    if (state.showPositionManage) {
        PositionManageDialog(
            positions = state.positions,
            onSave = viewModel::savePosition,
            onDelete = viewModel::deletePosition,
            onToggleActive = viewModel::setPositionActive,
            onDismiss = { viewModel.setDialog(showPositionManage = false) }
        )
    }
    if (state.showPresetManage) {
        PresetManageDialog(
            presets = state.presets,
            roles = state.roles,
            positions = state.positions,
            scheduleTemplates = state.scheduleTemplates,
            selectedPresetId = state.selectedPresetId,
            defaultShiftDurationMinutes = state.settings.defaultShiftDurationMinutes,
            onSelectPreset = viewModel::selectPreset,
            onAddPreset = { name -> viewModel.savePreset(0, name, null) },
            onSetDefault = viewModel::setDefaultPreset,
            onDeletePreset = viewModel::deletePreset,
            onSaveRole = viewModel::saveRole,
            onDeleteRole = viewModel::deleteRole,
            onToggleRoleActive = viewModel::setRoleActive,
            onDismiss = { viewModel.setDialog(showPresetManage = false) }
        )
    }
    if (state.showScheduleManage) {
        ScheduleManageDialog(
            templates = state.scheduleTemplates,
            onSave = viewModel::saveScheduleTemplate,
            onDelete = viewModel::deleteScheduleTemplate,
            onDismiss = { viewModel.setDialog(showScheduleManage = false) }
        )
    }
    if (state.showBreakEdit) {
        BreakEditDialog(
            breaks = state.breaks,
            onGenerateDefault = viewModel::generateDefaultBreaks,
            onUpdate = viewModel::updateBreak,
            onRemove = viewModel::removeBreak,
            onDismiss = { viewModel.setDialog(showBreakEdit = false) }
        )
    }
    if (state.showSettings) {
        RotationSettingsDialog(
            settings = state.settings,
            onSave = { viewModel.updateSettings(it); viewModel.setDialog(showSettings = false) },
            onDismiss = { viewModel.setDialog(showSettings = false) }
        )
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
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = fg)
        }
    }
}
