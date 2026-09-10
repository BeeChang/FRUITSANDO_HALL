package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.rotation.RotationPositionUi
import example.yf.fruit_hall.ui.rotation.RotationPresetUi
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.rotation.RotationScheduleTemplateUi
import example.yf.fruit_hall.ui.theme.AppTheme

private val presetTopTabs = listOf(
    R.string.rotation_preset_tab_preset,
    R.string.rotation_palette_position_title,
    R.string.rotation_palette_schedule_title
)

/**
 * 근무조 준비를 한 곳에서 끝내는 다이얼로그. 멤버는 전역이라 여기서 다루지 않고,
 * 프리셋(이름) · 포지션(전역) · 근무 스케줄(프리셋별)을 탭으로 묶어 순서대로 채워 넣을 수 있게 한다.
 */
@Composable
fun PresetManageDialog(
    presets: List<RotationPresetUi>,
    selectedPresetId: Long?,
    onSelectPreset: (Long) -> Unit,
    onAddPreset: (String) -> Unit,
    onRenamePreset: (Long, String) -> Unit,
    onSetDefault: (Long) -> Unit,
    onDeletePreset: (Long) -> Unit,
    positions: List<RotationPositionUi>,
    onSavePosition: (RotationPositionUi) -> Unit,
    onDeletePosition: (Long) -> Unit,
    onTogglePositionActive: (Long, Boolean) -> Unit,
    templates: List<RotationScheduleTemplateUi>,
    onSaveTemplate: (RotationScheduleTemplateUi) -> Unit,
    onDeleteTemplate: (RotationScheduleTemplateUi) -> Unit,
    onDismiss: () -> Unit,
    initialTab: Int = 0
) {
    val appColors = AppTheme.colors
    var newPresetName by remember { mutableStateOf("") }
    var topTab by remember { mutableIntStateOf(initialTab) }
    var editingPresetId by remember { mutableStateOf<Long?>(null) }
    var editingPresetName by remember { mutableStateOf("") }

    fun commitRename() {
        val id = editingPresetId
        if (id != null && editingPresetName.isNotBlank()) onRenamePreset(id, editingPresetName)
        editingPresetId = null
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.68f).heightIn(max = 920.dp),
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
                    Text(stringResource(R.string.rotation_preset_manage_title), style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.rotation_close), tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                TabRow(selectedTabIndex = topTab, containerColor = MaterialTheme.colorScheme.background, contentColor = appColors.primary700) {
                    presetTopTabs.forEachIndexed { index, titleRes ->
                        val selected = topTab == index
                        Tab(
                            selected = selected,
                            onClick = { topTab = index },
                            selectedContentColor = appColors.primary700,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            text = {
                                Text(stringResource(titleRes), style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        )
                    }
                }

                when (topTab) {
                    0 -> Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).weight(1f, fill = false).verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            stringResource(R.string.rotation_preset_desc),
                            style = MaterialTheme.typography.bodySmall, color = appColors.grey500
                        )
                        Spacer(Modifier.height(12.dp))

                        presets.forEach { preset ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = preset.id == selectedPresetId, onClick = { onSelectPreset(preset.id) })
                                if (editingPresetId == preset.id) {
                                    OutlinedTextField(
                                        value = editingPresetName, onValueChange = { editingPresetName = it },
                                        modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(8.dp)
                                    )
                                    ClickShrinkEffect(onClick = { commitRename() }) {
                                        Icon(
                                            Icons.Default.Check, contentDescription = stringResource(R.string.rotation_preset_rename_save),
                                            tint = appColors.primary500, modifier = Modifier.size(34.dp).padding(8.dp)
                                        )
                                    }
                                    ClickShrinkEffect(onClick = { editingPresetId = null }) {
                                        Icon(
                                            Icons.Default.Close, contentDescription = stringResource(R.string.rotation_cancel),
                                            tint = appColors.grey400, modifier = Modifier.size(34.dp).padding(8.dp)
                                        )
                                    }
                                } else {
                                    Text(preset.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    ClickShrinkEffect(onClick = { editingPresetId = preset.id; editingPresetName = preset.name }) {
                                        Icon(
                                            Icons.Default.Edit, contentDescription = stringResource(R.string.rotation_preset_rename),
                                            tint = appColors.grey500, modifier = Modifier.size(34.dp).padding(9.dp)
                                        )
                                    }
                                    IconButton(onClick = { onSetDefault(preset.id) }, modifier = Modifier.size(34.dp)) {
                                        Icon(
                                            if (preset.isDefault) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = stringResource(R.string.rotation_preset_set_default),
                                            tint = if (preset.isDefault) appColors.primary500 else appColors.grey400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(onClick = { onDeletePreset(preset.id) }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.rotation_delete), tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(17.dp))
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newPresetName, onValueChange = { newPresetName = it }, modifier = Modifier.weight(1f),
                                label = { Text(stringResource(R.string.rotation_preset_name_label)) }, singleLine = true, shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { if (newPresetName.isNotBlank()) { onAddPreset(newPresetName); newPresetName = "" } },
                                enabled = newPresetName.isNotBlank(), shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.rotation_preset_add))
                            }
                        }
                    }

                    1 -> PositionManageContent(
                        positions = positions,
                        onSave = onSavePosition,
                        onDelete = onDeletePosition,
                        onToggleActive = onTogglePositionActive,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).weight(1f, fill = false)
                    )

                    else -> ScheduleManageContent(
                        templates = templates,
                        onSaveTemplate = onSaveTemplate,
                        onDeleteTemplate = onDeleteTemplate,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).weight(1f, fill = false)
                    )
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.rotation_close), color = appColors.grey600) }
                }
            }
        }
    }
}
