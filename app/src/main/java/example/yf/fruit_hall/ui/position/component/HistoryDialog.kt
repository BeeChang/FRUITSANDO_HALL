package example.yf.fruit_hall.ui.position.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.ui.position.SlotHistoryUi

@Composable
fun HistoryDialog(
    history: List<SlotHistoryUi>,
    onResetToday: () -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDayResetConfirm by remember { mutableStateOf(false) }
    var showFullResetConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "오늘 기록",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (history.isEmpty()) {
                    Text(
                        text = "기록이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { slot ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "${slot.slotNumber}차",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    slot.assignments.forEach { assignment ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = assignment.memberName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "→ ${assignment.positionName}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showDayResetConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("오늘 초기화")
                    }
                    Button(
                        onClick = { showFullResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("전체 초기화")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = onDismiss) {
                        Text("닫기")
                    }
                }
            }
        }
    }

    if (showDayResetConfirm) {
        AlertDialog(
            onDismissRequest = { showDayResetConfirm = false },
            title = { Text("오늘 기록 초기화") },
            text = { Text("오늘의 배정 기록을 모두 삭제하고 차수를 1로 되돌립니다.\n계속하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDayResetConfirm = false
                        onResetToday()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("초기화") }
            },
            dismissButton = {
                TextButton(onClick = { showDayResetConfirm = false }) { Text("취소") }
            }
        )
    }

    if (showFullResetConfirm) {
        AlertDialog(
            onDismissRequest = { showFullResetConfirm = false },
            title = { Text("전체 기록 초기화") },
            text = { Text("모든 배정 기록을 삭제합니다.\n이 작업은 되돌릴 수 없습니다.\n계속하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        showFullResetConfirm = false
                        onResetAll()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("전체 초기화") }
            },
            dismissButton = {
                TextButton(onClick = { showFullResetConfirm = false }) { Text("취소") }
            }
        )
    }
}