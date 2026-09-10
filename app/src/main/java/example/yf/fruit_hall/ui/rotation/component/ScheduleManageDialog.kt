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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.rotation.RotationScheduleTemplateUi
import example.yf.fruit_hall.ui.rotation.formatMinutes
import example.yf.fruit_hall.ui.theme.AppTheme

/**
 * 재사용 근무 스케줄 템플릿 관리. 프리셋 관리 다이얼로그의 '근무 스케줄' 탭 안에 그대로 들어간다.
 * 여기서 만든 템플릿(오픈/미들/마감 등)은 프리셋과 무관하게 재사용되며, 로테이션 표의 팔레트에서
 * 끌어다 놓으면 그 줄의 출퇴근 시각과 기본 브레이크가 한 번에 적용된다.
 * 표에 들어갈 근무 줄 자체는 이 화면이 아니라 표에서 직접 추가·삭제한다.
 */
@Composable
fun ScheduleManageContent(
    templates: List<RotationScheduleTemplateUi>,
    onSaveTemplate: (RotationScheduleTemplateUi) -> Unit,
    onDeleteTemplate: (RotationScheduleTemplateUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors

    var label by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(10 * 60) }
    var end by remember { mutableStateOf(19 * 60) }
    var breakEnabled by remember { mutableStateOf(true) }
    var breakStart by remember { mutableStateOf(13 * 60 + 30) }
    var breakMinutesText by remember { mutableStateOf("60") }

    Column(modifier.verticalScroll(rememberScrollState())) {
        Text(
            stringResource(R.string.rotation_schedule_desc),
            style = MaterialTheme.typography.bodySmall, color = appColors.grey500
        )
        Spacer(Modifier.height(12.dp))

        if (templates.isNotEmpty()) {
            LazyColumn(Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(templates, key = { it.id }) { template ->
                    Row(
                        Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(template.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                stringResource(R.string.rotation_schedule_time_range, formatMinutes(template.startMin), formatMinutes(template.endMin)),
                                style = MaterialTheme.typography.labelSmall, color = appColors.grey600
                            )
                            val breakStartMin = template.breakStartMin
                            if (breakStartMin != null && template.breakMinutes > 0) {
                                Text(
                                    stringResource(R.string.rotation_schedule_break_range, formatMinutes(breakStartMin), formatMinutes(breakStartMin + template.breakMinutes)),
                                    style = MaterialTheme.typography.labelSmall, color = appColors.crimson400
                                )
                            }
                        }
                        IconButton(onClick = { onDeleteTemplate(template) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.rotation_delete), tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Text(stringResource(R.string.rotation_schedule_add_new), style = MaterialTheme.typography.labelMedium, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = label, onValueChange = { label = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.rotation_schedule_name_label)) }, singleLine = true, shape = RoundedCornerShape(8.dp)
        )
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RotationTimePickerField(label = stringResource(R.string.rotation_role_start_label), minutes = start, onChange = { start = it }, modifier = Modifier.weight(1f))
            RotationTimePickerField(label = stringResource(R.string.rotation_role_end_label), minutes = end, onChange = { end = it }, modifier = Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.rotation_schedule_break_section), style = MaterialTheme.typography.labelMedium, color = appColors.grey700, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Switch(
                checked = breakEnabled, onCheckedChange = { breakEnabled = it },
                colors = SwitchDefaults.colors(checkedTrackColor = appColors.crimson400)
            )
        }
        if (breakEnabled) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RotationTimePickerField(label = stringResource(R.string.rotation_schedule_break_start), minutes = breakStart, onChange = { breakStart = it }, modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = breakMinutesText,
                    onValueChange = { v -> breakMinutesText = v.filter { it.isDigit() }.take(3) },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.rotation_role_break_minutes_label)) }, singleLine = true, shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = {
                    if (label.isBlank()) return@Button
                    onSaveTemplate(
                        RotationScheduleTemplateUi(
                            id = 0, label = label, startMin = start, endMin = end,
                            breakStartMin = if (breakEnabled) breakStart else null,
                            breakMinutes = if (breakEnabled) (breakMinutesText.toIntOrNull() ?: 60) else 0,
                            sortOrder = templates.size
                        )
                    )
                    label = ""
                },
                enabled = label.isNotBlank(), shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.rotation_schedule_add))
            }
        }
    }
}
