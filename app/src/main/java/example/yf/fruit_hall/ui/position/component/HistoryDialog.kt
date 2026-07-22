package example.yf.fruit_hall.ui.position.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.ui.position.SlotHistoryUi
import example.yf.fruit_hall.ui.theme.AppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val historyDateFmt = DateTimeFormatter.ofPattern("yyyy년 M월 d일")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDialog(
    history: List<SlotHistoryUi>,
    viewDate: String,
    todayDate: String,
    availableDates: Set<String>,
    showDatePicker: Boolean,
    onOpenDatePicker: () -> Unit,
    onCloseDatePicker: () -> Unit,
    onSelectDate: (String) -> Unit,
    onResetToday: () -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var showDayResetConfirm by remember { mutableStateOf(false) }
    var showFullResetConfirm by remember { mutableStateOf(false) }
    val isViewingToday = viewDate.isEmpty() || viewDate == todayDate
    val viewDateLabel = remember(viewDate) {
        runCatching { LocalDate.parse(viewDate).format(historyDateFmt) }.getOrDefault(viewDate)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.75f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = appColors.white,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isViewingToday) "오늘 배정 기록" else "$viewDateLabel 배정 기록",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.white,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onOpenDatePicker, enabled = availableDates.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "날짜 선택",
                            tint = if (availableDates.isNotEmpty()) appColors.grey300 else appColors.grey600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = appColors.grey300,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = appColors.grey200,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = if (isViewingToday) "오늘 배정 기록이 없습니다" else "이 날짜엔 배정 기록이 없습니다",
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.grey400
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(history) { slot ->
                            SlotHistoryCard(slot = slot)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isViewingToday) {
                        TextButton(
                            onClick = { showDayResetConfirm = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = appColors.crimson500)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("오늘 초기화", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    TextButton(
                        onClick = { showFullResetConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = appColors.crimson500)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("전체 초기화", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("닫기")
                    }
                }
            }
        }
    }

    if (showDayResetConfirm) {
        AlertDialog(
            onDismissRequest = { showDayResetConfirm = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("오늘 기록 초기화", fontWeight = FontWeight.SemiBold) },
            text = { Text("오늘의 배정 기록을 모두 삭제하고 차수를 1로 되돌립니다.") },
            confirmButton = {
                Button(
                    onClick = { showDayResetConfirm = false; onResetToday(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.crimson500),
                    shape = RoundedCornerShape(8.dp)
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
            shape = RoundedCornerShape(16.dp),
            title = { Text("전체 기록 초기화", fontWeight = FontWeight.SemiBold) },
            text = { Text("모든 배정 기록을 영구 삭제합니다.\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = { showFullResetConfirm = false; onResetAll(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.crimson500),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("전체 초기화") }
            },
            dismissButton = {
                TextButton(onClick = { showFullResetConfirm = false }) { Text("취소") }
            }
        )
    }

    if (showDatePicker) {
        val availableEpochDays = remember(availableDates) {
            availableDates.mapNotNull { runCatching { LocalDate.parse(it).toEpochDay() }.getOrNull() }.toSet()
        }
        val initialMillis = remember(viewDate) {
            runCatching {
                LocalDate.parse(viewDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }.getOrNull()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val epochDay = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    return epochDay in availableEpochDays
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = onCloseDatePicker,
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        onSelectDate(date)
                    } else {
                        onCloseDatePicker()
                    }
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = onCloseDatePicker) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private val slotPaletteColors = listOf(
    Color(0xFFFFB3C6),
    Color(0xFFB3D9FF),
    Color(0xFFA8E6CF),
    Color(0xFFD4B8F5),
    Color(0xFFFFD6A5),
)

@Composable
private fun SlotHistoryCard(slot: SlotHistoryUi) {
    val appColors = AppTheme.colors
    val slotColor = slotPaletteColors[(slot.slotNumber - 1) % slotPaletteColors.size]
    val textColor = Color(0xFF2D2D2D)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = slotColor.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(slotColor)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${slot.slotNumber}차",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = appColors.success500,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "확정",
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors.success600,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            slot.assignments.forEach { assignment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = assignment.memberName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.grey300
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(slotColor)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = assignment.positionName,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
