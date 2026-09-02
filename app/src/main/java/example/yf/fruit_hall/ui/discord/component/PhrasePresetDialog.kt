package example.yf.fruit_hall.ui.discord.component

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Save
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
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.discord.DiscordPhrasePresetUi
import example.yf.fruit_hall.ui.theme.AppTheme

/** 문구 여러 개를 순서대로 묶은 프리셋을 골라 조합 목록에 한 번에 불러오거나, 현재 조합을 새 프리셋으로 저장한다. */
@Composable
fun PhrasePresetDialog(
    presets: List<DiscordPhrasePresetUi>,
    canSaveCurrent: Boolean,
    onPick: (DiscordPhrasePresetUi) -> Unit,
    onSaveCurrent: (String) -> Unit,
    onDelete: (DiscordPhrasePresetUi) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var newPresetName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.55f).heightIn(max = 620.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.warning500)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Collections, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("문구 프리셋", style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.white.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }

                Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text("눌러서 조합 목록 맨 뒤에 한 번에 추가합니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey500)
                    Spacer(Modifier.height(10.dp))

                    if (presets.isEmpty()) {
                        Text("저장된 프리셋이 없습니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                    } else {
                        LazyColumn(Modifier.heightIn(max = 260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(presets, key = { it.id }) { preset ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ClickShrinkEffect(shrinkFactor = 0.98f, onClick = { onPick(preset) }, modifier = Modifier.weight(1f)) {
                                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                            Text(preset.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            Text(
                                                "문구 ${preset.items.size}개 · " + preset.items.joinToString(" / ").let { if (it.length > 40) it.take(40) + "…" else it },
                                                style = MaterialTheme.typography.labelSmall, color = appColors.grey500
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onDelete(preset) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "삭제", tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Text("현재 조합을 새 프리셋으로 저장", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    if (!canSaveCurrent) {
                        Text("조합 목록이 비어있으면 저장할 수 없습니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newPresetName, onValueChange = { newPresetName = it }, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("예: 산도 라인업") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { if (newPresetName.isNotBlank()) { onSaveCurrent(newPresetName); newPresetName = "" } },
                        enabled = canSaveCurrent && newPresetName.isNotBlank(), shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("프리셋으로 저장")
                    }
                }

                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("닫기", color = appColors.grey600) }
                }
            }
        }
    }
}
