package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FreeBreakfast
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
import example.yf.fruit_hall.ui.rotation.BreakUi
import example.yf.fruit_hall.ui.rotation.formatMinutes
import example.yf.fruit_hall.ui.rotation.parseMinutes
import example.yf.fruit_hall.ui.theme.AppTheme

@Composable
fun BreakEditDialog(
    breaks: List<BreakUi>,
    onGenerateDefault: () -> Unit,
    onUpdate: (memberId: Long, startMin: Int, endMin: Int) -> Unit,
    onRemove: (memberId: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.58f).heightIn(max = 700.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FreeBreakfast, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("브레이크 편집", style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).weight(1f, fill = false)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("겹쳐도 되고 자유롭게 옮길 수 있어요", style = MaterialTheme.typography.labelSmall, color = appColors.grey500, modifier = Modifier.weight(1f))
                        TextButton(onClick = onGenerateDefault) { Text("기본 브레이크 생성") }
                    }
                    Spacer(Modifier.height(8.dp))

                    if (breaks.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                            Text("등록된 브레이크가 없어요", style = MaterialTheme.typography.bodyMedium, color = appColors.grey400)
                        }
                    }

                    LazyColumn(Modifier.weight(1f, fill = false)) {
                        items(breaks, key = { it.memberId }) { b ->
                            var startText by remember(b.memberId) { mutableStateOf(formatMinutes(b.startMin)) }
                            var endText by remember(b.memberId) { mutableStateOf(formatMinutes(b.endMin)) }
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(b.memberName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                OutlinedTextField(
                                    value = startText, onValueChange = { startText = it }, modifier = Modifier.width(90.dp),
                                    label = { Text("시작") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                OutlinedTextField(
                                    value = endText, onValueChange = { endText = it }, modifier = Modifier.width(90.dp),
                                    label = { Text("종료") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Button(
                                    onClick = {
                                        val s = parseMinutes(startText); val e = parseMinutes(endText)
                                        if (s != null && e != null) onUpdate(b.memberId, s, e)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("저장") }
                                IconButton(onClick = { onRemove(b.memberId) }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) { Text("확인") }
                }
            }
        }
    }
}
