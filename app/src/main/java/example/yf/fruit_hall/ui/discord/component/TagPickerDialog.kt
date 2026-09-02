package example.yf.fruit_hall.ui.discord.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Close
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
import example.yf.fruit_hall.ui.discord.DiscordTagUi
import example.yf.fruit_hall.ui.theme.AppTheme

/** 저장된 디스코드 태그(멘션/역할 호출자)를 골라 메시지에 끌어다 쓰거나, 새 태그를 등록·삭제한다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagPickerDialog(
    tags: List<DiscordTagUi>,
    onPick: (DiscordTagUi) -> Unit,
    onSave: (String) -> Unit,
    onDelete: (DiscordTagUi) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var newTag by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.5f).heightIn(max = 560.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.secondary500)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("태그 선택", style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.white.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }

                Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text("눌러서 메시지에 추가합니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey500)
                    Spacer(Modifier.height(10.dp))

                    if (tags.isEmpty()) {
                        Text("저장된 태그가 없습니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tags.forEach { tag ->
                                Row(
                                    Modifier
                                        .background(appColors.secondary100, RoundedCornerShape(50))
                                        .padding(end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    example.yf.fruit_hall.ui.component.util.ClickShrinkEffect(shrinkFactor = 0.95f, onClick = { onPick(tag) }) {
                                        Text(
                                            tag.text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium,
                                            color = appColors.secondary700,
                                            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
                                        )
                                    }
                                    IconButton(onClick = { onDelete(tag) }, modifier = Modifier.size(22.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "삭제", tint = appColors.secondary700.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Text("새 태그 추가", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newTag, onValueChange = { newTag = it }, modifier = Modifier.weight(1f),
                            placeholder = { Text("예: @here") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { if (newTag.isNotBlank()) { onSave(newTag); newTag = "" } },
                            enabled = newTag.isNotBlank(), shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("닫기", color = appColors.grey600) }
                }
            }
        }
    }
}
