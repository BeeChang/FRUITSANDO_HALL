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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatQuote
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
import example.yf.fruit_hall.ui.discord.DiscordPhraseUi
import example.yf.fruit_hall.ui.theme.AppTheme

/** 저장된 문구를 골라 메시지 조합 목록에 끌어다 쓰거나, 새 문구를 등록·삭제한다. */
@Composable
fun PhrasePickerDialog(
    phrases: List<DiscordPhraseUi>,
    onPick: (DiscordPhraseUi) -> Unit,
    onSave: (String) -> Unit,
    onDelete: (DiscordPhraseUi) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var newPhrase by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.55f).heightIn(max = 620.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.primary500)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FormatQuote, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("저장 문구 선택", style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.white.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }

                Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text("눌러서 메시지 조합 목록에 추가합니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey500)
                    Spacer(Modifier.height(10.dp))

                    if (phrases.isEmpty()) {
                        Text("저장된 문구가 없습니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                    } else {
                        LazyColumn(Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(phrases, key = { it.id }) { phrase ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ClickShrinkEffect(shrinkFactor = 0.98f, onClick = { onPick(phrase) }, modifier = Modifier.weight(1f)) {
                                        Text(
                                            phrase.text, style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            maxLines = 3
                                        )
                                    }
                                    IconButton(onClick = { onDelete(phrase) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "삭제", tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Text("새 문구 추가", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhrase, onValueChange = { newPhrase = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp),
                        placeholder = { Text("자주 쓰는 문장을 입력하세요") }, shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { if (newPhrase.isNotBlank()) { onSave(newPhrase); newPhrase = "" } },
                        enabled = newPhrase.isNotBlank(), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("문구 저장")
                    }
                }

                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("닫기", color = appColors.grey600) }
                }
            }
        }
    }
}
