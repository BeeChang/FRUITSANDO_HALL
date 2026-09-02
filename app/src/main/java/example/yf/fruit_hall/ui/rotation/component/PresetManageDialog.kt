package example.yf.fruit_hall.ui.rotation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.ui.rotation.RotationPositionUi
import example.yf.fruit_hall.ui.rotation.RotationPresetUi
import example.yf.fruit_hall.ui.rotation.RotationRoleUi
import example.yf.fruit_hall.ui.rotation.RotationScheduleTemplateUi
import example.yf.fruit_hall.ui.rotation.formatMinutes
import example.yf.fruit_hall.ui.theme.AppTheme

private val RolePalette = listOf(Color(0xFF5B8DEF), Color(0xFFE07A5F), Color(0xFF43AA8B), Color(0xFFB185DB), Color(0xFFE0A458))

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetManageDialog(
    presets: List<RotationPresetUi>,
    roles: List<RotationRoleUi>,
    positions: List<RotationPositionUi>,
    scheduleTemplates: List<RotationScheduleTemplateUi>,
    selectedPresetId: Long?,
    defaultShiftDurationMinutes: Int,
    onSelectPreset: (Long) -> Unit,
    onAddPreset: (String) -> Unit,
    onSetDefault: (Long) -> Unit,
    onDeletePreset: (Long) -> Unit,
    onSaveRole: (RotationRoleUi) -> Unit,
    onDeleteRole: (RotationRoleUi) -> Unit,
    onToggleRoleActive: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    var newPresetName by remember { mutableStateOf("") }
    var editingRoleId by remember { mutableStateOf<Long?>(null) }
    var roleLabel by remember { mutableStateOf("") }
    var roleStart by remember { mutableStateOf(11 * 60) }
    var roleEnd by remember { mutableStateOf(19 * 60) }
    var endTouched by remember { mutableStateOf(false) }
    var roleBreakOrder by remember { mutableStateOf("") }
    var roleBreakMinutes by remember { mutableStateOf("60") }

    fun resetForm() {
        editingRoleId = null
        roleLabel = ""
        roleStart = 11 * 60
        roleEnd = 19 * 60
        endTouched = false
        roleBreakOrder = ""
        roleBreakMinutes = "60"
    }

    fun startEdit(role: RotationRoleUi) {
        editingRoleId = role.id
        roleLabel = role.label
        roleStart = role.startMin
        roleEnd = role.endMin
        endTouched = true
        roleBreakOrder = role.breakOrder?.toString() ?: ""
        roleBreakMinutes = role.breakMinutes.toString()
    }

    val presetListSection: @Composable () -> Unit = {
        presets.forEach { preset ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = preset.id == selectedPresetId, onClick = { onSelectPreset(preset.id) })
                Text(preset.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                IconButton(onClick = { onSetDefault(preset.id) }, modifier = Modifier.size(34.dp)) {
                    Icon(
                        if (preset.isDefault) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "기본 프리셋으로 지정",
                        tint = if (preset.isDefault) appColors.primary500 else appColors.grey400,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = { onDeletePreset(preset.id) }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(17.dp))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newPresetName, onValueChange = { newPresetName = it }, modifier = Modifier.weight(1f),
                label = { Text("프리셋 이름") }, singleLine = true, shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { if (newPresetName.isNotBlank()) { onAddPreset(newPresetName); newPresetName = "" } },
                enabled = newPresetName.isNotBlank(), shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("프리셋 추가")
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)
        Text("근무 스케줄 목록", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(roles) { index, role ->
                val accent = RolePalette[index % RolePalette.size]
                Row(
                    Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (role.isActive) 1f else 0.4f), RoundedCornerShape(10.dp))
                        .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .padding(start = 6.dp, end = 10.dp)
                            .size(8.dp)
                            .background(accent, RoundedCornerShape(50))
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            role.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                            color = if (role.isActive) MaterialTheme.colorScheme.onSurface else appColors.grey500
                        )
                        Spacer(Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${formatMinutes(role.startMin)}~${formatMinutes(role.endMin)}",
                                style = MaterialTheme.typography.labelSmall, color = appColors.grey600
                            )
                            if (role.breakOrder != null) {
                                Spacer(Modifier.width(6.dp))
                                Row(
                                    Modifier.background(appColors.secondary100, RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.FreeBreakfast, contentDescription = null, tint = appColors.secondary700, modifier = Modifier.size(10.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text("${role.breakOrder}번 ${role.breakMinutes}분", style = MaterialTheme.typography.labelSmall, color = appColors.secondary700)
                                }
                            }
                        }
                    }
                    Switch(
                        checked = role.isActive, onCheckedChange = { onToggleRoleActive(role.id, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = appColors.primary500)
                    )
                    IconButton(onClick = { startEdit(role) }, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "수정", tint = appColors.primary500, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { if (editingRoleId == role.id) resetForm(); onDeleteRole(role) }, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }

    val roleFormSection: @Composable () -> Unit = {
        Text(
            if (editingRoleId != null) "근무 스케줄 수정" else "근무 스케줄 추가",
            style = MaterialTheme.typography.labelLarge, color = appColors.primary700, fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        Text("포지션에서 이름 선택", style = MaterialTheme.typography.labelSmall, color = appColors.grey600)
        Spacer(Modifier.height(6.dp))
        if (positions.isEmpty()) {
            Text("먼저 '포지션 관리'에서 매대 포지션을 추가해주세요", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                positions.forEach { position ->
                    FilterChip(
                        selected = roleLabel == position.name,
                        onClick = {
                            val existingLabels = roles.map { it.label }.toSet()
                            var candidate = position.name
                            var suffix = 2
                            while (candidate in existingLabels && candidate != roleLabel) {
                                candidate = "${position.name} $suffix"; suffix++
                            }
                            roleLabel = candidate
                        },
                        label = { Text(position.name, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = appColors.primary100, selectedLabelColor = appColors.primary700)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("스케줄에서 시간·브레이크 모양 선택", style = MaterialTheme.typography.labelSmall, color = appColors.grey600)
        Spacer(Modifier.height(6.dp))
        if (scheduleTemplates.isEmpty()) {
            Text("먼저 '스케줄 관리'에서 근무 스케줄을 추가해주세요", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                scheduleTemplates.forEach { template ->
                    FilterChip(
                        selected = roleStart == template.startMin && roleEnd == template.endMin,
                        onClick = {
                            roleStart = template.startMin
                            roleEnd = template.endMin
                            endTouched = true
                            roleBreakOrder = template.breakOrder?.toString() ?: ""
                            roleBreakMinutes = template.breakMinutes.toString()
                        },
                        label = { Text("${template.label} (${formatMinutes(template.startMin)}~${formatMinutes(template.endMin)})", style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = appColors.success100, selectedLabelColor = appColors.success700)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = roleLabel, onValueChange = { roleLabel = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("근무 스케줄 이름 (위에서 선택하거나 직접 입력)") }, singleLine = true, shape = RoundedCornerShape(8.dp)
        )
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RotationTimePickerField(
                label = "출근", minutes = roleStart,
                onChange = { roleStart = it; if (!endTouched) roleEnd = (it + defaultShiftDurationMinutes).coerceAtMost(29 * 60 + 59) },
                modifier = Modifier.weight(1f)
            )
            RotationTimePickerField(
                label = "퇴근", minutes = roleEnd,
                onChange = { roleEnd = it; endTouched = true },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = roleBreakOrder, onValueChange = { roleBreakOrder = it }, modifier = Modifier.weight(1f),
                label = { Text("브레이크 순번(비우면 없음)") }, singleLine = true, shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = roleBreakMinutes, onValueChange = { roleBreakMinutes = it }, modifier = Modifier.weight(1f),
                label = { Text("브레이크 길이(분)") }, singleLine = true, shape = RoundedCornerShape(8.dp)
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
            if (editingRoleId != null) {
                TextButton(onClick = { resetForm() }) { Text("수정 취소", color = appColors.grey600) }
                Spacer(Modifier.width(4.dp))
            }
            Button(
                onClick = {
                    val presetId = selectedPresetId ?: return@Button
                    if (roleLabel.isBlank()) return@Button
                    onSaveRole(
                        RotationRoleUi(
                            id = editingRoleId ?: 0, presetId = presetId, label = roleLabel, startMin = roleStart, endMin = roleEnd,
                            breakOrder = roleBreakOrder.toIntOrNull(), breakMinutes = roleBreakMinutes.toIntOrNull() ?: 60,
                            sortOrder = roles.size
                        )
                    )
                    resetForm()
                },
                enabled = selectedPresetId != null && roleLabel.isNotBlank(), shape = RoundedCornerShape(8.dp)
            ) {
                Icon(if (editingRoleId != null) Icons.Default.Edit else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (editingRoleId != null) "수정 저장" else "근무 스케줄 추가")
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = if (isPortrait) Modifier.fillMaxWidth(0.62f).heightIn(max = 720.dp) else Modifier.fillMaxWidth(0.86f).heightIn(max = 560.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("근무조 프리셋 관리", style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                if (isPortrait) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        presetListSection()
                        HorizontalDivider(Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        roleFormSection()
                    }
                } else {
                    Row(Modifier.weight(1f, fill = false).padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Column(
                            Modifier.weight(0.45f).fillMaxHeight().verticalScroll(rememberScrollState())
                        ) {
                            roleFormSection()
                        }
                        Spacer(Modifier.width(20.dp))
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.width(20.dp))
                        Column(
                            Modifier.weight(0.55f).fillMaxHeight().verticalScroll(rememberScrollState())
                        ) {
                            presetListSection()
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("닫기", color = appColors.grey600) }
                }
            }
        }
    }
}
