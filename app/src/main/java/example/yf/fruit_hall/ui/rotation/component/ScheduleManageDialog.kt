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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.ui.rotation.RotationScheduleTemplateUi
import example.yf.fruit_hall.ui.rotation.formatMinutes
import example.yf.fruit_hall.ui.theme.AppTheme

/** 여러 프리셋에서 끌어다 쓸 수 있는 재사용 근무 스케줄(시작·종료·브레이크) 템플릿 관리. */
@Composable
fun ScheduleManageDialog(
    templates: List<RotationScheduleTemplateUi>,
    onSave: (RotationScheduleTemplateUi) -> Unit,
    onDelete: (RotationScheduleTemplateUi) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var label by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(10 * 60) }
    var end by remember { mutableStateOf(19 * 60) }
    var breakOrder by remember { mutableStateOf("") }
    var breakMinutes by remember { mutableStateOf("60") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.55f).heightIn(max = 640.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.success500)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("스케줄 관리", style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.white.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }

                Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        "출근·퇴근·브레이크 모양을 미리 만들어두면 프리셋 관리에서 골라 쓸 수 있습니다",
                        style = MaterialTheme.typography.bodySmall, color = appColors.grey500
                    )
                    Spacer(Modifier.height(12.dp))

                    if (templates.isNotEmpty()) {
                        LazyColumn(Modifier.heightIn(max = 260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(templates, key = { it.id }) { template ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                        .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(template.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "${formatMinutes(template.startMin)}~${formatMinutes(template.endMin)}",
                                                style = MaterialTheme.typography.labelSmall, color = appColors.grey600
                                            )
                                            if (template.breakOrder != null) {
                                                Spacer(Modifier.width(6.dp))
                                                Icon(Icons.Default.FreeBreakfast, contentDescription = null, tint = appColors.success500, modifier = Modifier.size(11.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text("${template.breakOrder}번 ${template.breakMinutes}분", style = MaterialTheme.typography.labelSmall, color = appColors.success700)
                                            }
                                        }
                                    }
                                    IconButton(onClick = { onDelete(template) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    Text("새 스케줄 추가", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = label, onValueChange = { label = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("스케줄 이름 (예: 오픈, 미들, 마감)") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                    )
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RotationTimePickerField(label = "출근", minutes = start, onChange = { start = it }, modifier = Modifier.weight(1f))
                        RotationTimePickerField(label = "퇴근", minutes = end, onChange = { end = it }, modifier = Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = breakOrder, onValueChange = { breakOrder = it }, modifier = Modifier.weight(1f),
                            label = { Text("브레이크 순번(비우면 없음)") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = breakMinutes, onValueChange = { breakMinutes = it }, modifier = Modifier.weight(1f),
                            label = { Text("브레이크 길이(분)") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("닫기", color = appColors.grey600) }
                    Spacer(Modifier.width(4.dp))
                    Button(
                        onClick = {
                            if (label.isBlank()) return@Button
                            onSave(
                                RotationScheduleTemplateUi(
                                    id = 0, label = label, startMin = start, endMin = end,
                                    breakOrder = breakOrder.toIntOrNull(), breakMinutes = breakMinutes.toIntOrNull() ?: 60,
                                    sortOrder = templates.size
                                )
                            )
                            label = ""
                        },
                        enabled = label.isNotBlank(), shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("스케줄 추가")
                    }
                }
            }
        }
    }
}
