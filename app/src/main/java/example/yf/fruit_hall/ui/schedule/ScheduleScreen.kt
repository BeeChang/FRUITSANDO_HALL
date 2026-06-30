package example.yf.fruit_hall.ui.schedule

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.theme.AppTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ScheduleAccent  = Color(0xFF8B70C8)
private val ScheduleBgLight = Color(0xFFF3EFFE)
private val WeekendSat      = Color(0xFF6196FD)
private val WeekendSun      = Color(0xFFE05F80)

private val shiftChipColor = mapOf(
    "오픈"    to Color(0xFFB8EDCC),
    "오픈미들" to Color(0xFFAED9F5),
    "미들"    to Color(0xFFCDBEEE),
    "마감"    to Color(0xFFF5B8C4),
)
private val ChipTextColor  = Color(0xFF1A1A1A)
private val LABEL_WIDTH_LG = 62.dp   // 가로모드

private val WEEKDAY_LABELS = listOf("월", "화", "수", "목", "금", "토", "일")
private val SHIFT_ORDER    = listOf("오픈", "오픈미들", "미들", "마감")
private val dateFmt        = DateTimeFormatter.ofPattern("yyyyMMdd")

private fun formatDayHeader(date: String): String {
    val ld  = LocalDate.parse(date, dateFmt)
    val dow = when (ld.dayOfWeek) {
        DayOfWeek.MONDAY    -> "월"
        DayOfWeek.TUESDAY   -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY  -> "목"
        DayOfWeek.FRIDAY    -> "금"
        DayOfWeek.SATURDAY  -> "토"
        DayOfWeek.SUNDAY    -> "일"
        else                -> ""
    }
    return "${ld.monthValue}월 ${ld.dayOfMonth}일 ($dow)"
}

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val colors      = AppTheme.colors
    val listState   = rememberLazyListState()
    val isPortrait  = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }

    LaunchedEffect(uiState.currentMonthKey, uiState.isLoading) {
        if (!uiState.isLoading) {
            if (uiState.isCurrentMonth && uiState.todayWeekIndex >= 0) {
                listState.scrollToItem(uiState.todayWeekIndex)
            } else {
                listState.scrollToItem(0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.grey50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 헤더 카드 ──
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClickShrinkEffect(onClick = viewModel::onPrevMonth) {
                        Icon(Icons.Default.ChevronLeft, "이전 달",
                            modifier = Modifier.size(28.dp), tint = ScheduleAccent)
                    }
                    Text(
                        text = uiState.displayMonth,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ScheduleAccent,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    ClickShrinkEffect(onClick = viewModel::onNextMonth) {
                        Icon(Icons.Default.ChevronRight, "다음 달",
                            modifier = Modifier.size(28.dp), tint = ScheduleAccent)
                    }
                }
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp, color = ScheduleAccent
                        )
                    } else {
                        ClickShrinkEffect(onClick = viewModel::onRefresh) {
                            Icon(Icons.Default.Refresh, "새로고침",
                                modifier = Modifier.size(20.dp), tint = colors.grey400)
                        }
                    }
                }
            }
        }

        // ── 달력 카드 ──
        Card(
            modifier  = Modifier.fillMaxWidth().weight(1f),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = ScheduleAccent)
                }
                !uiState.hasData  -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(20.dp))
                        Text("업로드된 스케줄이 없습니다",
                            style = MaterialTheme.typography.titleMedium, color = colors.grey500)
                        Spacer(Modifier.height(6.dp))
                        Text(uiState.displayMonth,
                            style = MaterialTheme.typography.bodyMedium, color = colors.grey400)
                    }
                }
                else -> Column {
                    WeekdayHeader()
                    HorizontalDivider(color = Color(0x18000000), thickness = 0.5.dp)
                    CalendarGrid(
                        weeks        = uiState.weeks,
                        listState    = listState,
                        isPortrait   = isPortrait,
                        onDateTapped = viewModel::onDateTapped,
                        onShowDetail = { selectedDay = it }
                    )
                }
            }
        }
    }

    // 날짜 상세 다이얼로그
    selectedDay?.let { day ->
        DayDetailDialog(day = day, onDismiss = { selectedDay = null })
    }
}

@Composable
private fun WeekdayHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScheduleBgLight)
            .padding(vertical = 10.dp)
    ) {
        WEEKDAY_LABELS.forEachIndexed { idx, label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = when (idx) { 5 -> WeekendSat; 6 -> WeekendSun; else -> ScheduleAccent }
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    weeks: List<List<CalendarDay?>>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isPortrait: Boolean,
    onDateTapped: (String) -> Unit,
    onShowDetail: (CalendarDay) -> Unit
) {
    val cellHeight = if (isPortrait) 165.dp else 145.dp
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(weeks) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEachIndexed { colIdx, day ->
                    DayCell(
                        day          = day,
                        colIdx       = colIdx,
                        isPortrait   = isPortrait,
                        onDateTapped = onDateTapped,
                        onShowDetail = onShowDetail,
                        modifier     = Modifier.weight(1f)
                    )
                    if (colIdx < 6) {
                        Box(Modifier.width(0.5.dp).height(cellHeight).background(Color(0x0C000000)))
                    }
                }
            }
            HorizontalDivider(color = Color(0x0C000000), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay?,
    colIdx: Int,
    isPortrait: Boolean,
    onDateTapped: (String) -> Unit,
    onShowDetail: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors   = AppTheme.colors
    val isSatCol = colIdx == 5
    val isSunCol = colIdx == 6
    val faded    = (day?.isTrailing == true || day?.isPast == true) && day?.isRevealed != true
    val alpha    = if (faded) 0.28f else 1f
    val cellHeight = if (isPortrait) 165.dp else 145.dp

    val bgColor = when {
        day == null          -> colors.grey20
        day.isToday          -> ScheduleBgLight
        isSatCol || isSunCol -> Color(0xFFFCFBFF)
        else                 -> colors.white
    }

    Box(
        modifier = modifier
            .height(cellHeight)
            .background(bgColor)
            .then(if (day != null) Modifier.clickable { onDateTapped(day.date) } else Modifier)
            .padding(horizontal = if (isPortrait) 5.dp else 8.dp, vertical = 7.dp)
    ) {
        if (day == null) return@Box

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            // 날짜 행: 숫자 + 오늘 표시 + 돋보기 아이콘
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .then(
                            if (day.isToday)
                                Modifier
                                    .border(1.5.dp, ScheduleAccent.copy(alpha = 0.7f), CircleShape)
                                    .background(ScheduleBgLight, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            day.isToday -> ScheduleAccent
                            isSatCol    -> WeekendSat.copy(alpha = alpha)
                            isSunCol    -> WeekendSun.copy(alpha = alpha)
                            else        -> colors.grey800.copy(alpha = alpha)
                        }
                    )
                }
                if (day.isToday) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "오늘",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ScheduleAccent.copy(alpha = 0.75f)
                    )
                }
                Spacer(Modifier.weight(1f))
                // 돋보기 아이콘
                if (day.shifts.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "상세보기",
                        modifier = Modifier
                            .size(15.dp)
                            .clickable { onShowDetail(day) },
                        tint = colors.grey400.copy(alpha = alpha)
                    )
                }
            }

            // 근무 그룹
            val grouped = day.shifts.groupBy { it.shift }
            SHIFT_ORDER.forEach { shiftType ->
                val persons = grouped[shiftType] ?: return@forEach
                if (isPortrait) {
                    ShiftGroupPortrait(shiftType, persons, alpha)
                } else {
                    ShiftGroupLandscape(shiftType, persons, alpha)
                }
            }
        }
    }
}

/** 가로모드: [오픈 :] [칩] [칩] 한 행 */
@Composable
private fun ShiftGroupLandscape(shiftType: String, persons: List<ShiftEntry>, alpha: Float) {
    val chipBg = (shiftChipColor[shiftType] ?: Color(0xFFE0E0E0)).copy(alpha = alpha)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = "$shiftType :",
            modifier = Modifier.width(LABEL_WIDTH_LG).padding(top = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.grey700.copy(alpha = alpha),
            maxLines = 1
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            persons.forEach { PersonChip(it.personName, chipBg, alpha, large = true) }
        }
    }
}

/** 세로모드: 라벨 위, 칩 아래로 줄바꿈 */
@Composable
private fun ShiftGroupPortrait(shiftType: String, persons: List<ShiftEntry>, alpha: Float) {
    val chipBg = (shiftChipColor[shiftType] ?: Color(0xFFE0E0E0)).copy(alpha = alpha)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "$shiftType",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.grey600.copy(alpha = alpha),
            maxLines = 1
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            persons.forEach { PersonChip(it.personName, chipBg, alpha, large = false) }
        }
    }
}

@Composable
private fun PersonChip(name: String, bg: Color, alpha: Float, large: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .padding(
                horizontal = if (large) 7.dp else 5.dp,
                vertical   = if (large) 3.dp else 2.dp
            )
    ) {
        Text(
            text = name,
            style = if (large) MaterialTheme.typography.labelMedium
                    else MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ChipTextColor.copy(alpha = alpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── 날짜 상세 다이얼로그 ──────────────────────────────────────
@Composable
private fun DayDetailDialog(day: CalendarDay, onDismiss: () -> Unit) {
    val colors = AppTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier  = Modifier.fillMaxWidth(0.55f),
            shape     = RoundedCornerShape(24.dp),
            colors    = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                // 헤더
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (day.isToday) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ScheduleAccent)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("오늘", style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Text(
                        text = formatDayHeader(day.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.grey900
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0x15000000))
                Spacer(Modifier.height(16.dp))

                if (day.shifts.isEmpty()) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("스케줄 없음", style = MaterialTheme.typography.bodyMedium,
                            color = colors.grey400)
                    }
                } else {
                    val grouped = day.shifts.groupBy { it.shift }
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SHIFT_ORDER.forEach { shiftType ->
                            val persons = grouped[shiftType] ?: return@forEach
                            DetailShiftRow(shiftType = shiftType, persons = persons)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailShiftRow(shiftType: String, persons: List<ShiftEntry>) {
    val colors = AppTheme.colors
    val chipBg = shiftChipColor[shiftType] ?: Color(0xFFE0E0E0)

    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$shiftType :",
            modifier = Modifier.width(72.dp).padding(top = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.grey700
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            persons.forEach { entry ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = entry.personName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ChipTextColor
                    )
                }
            }
        }
    }
}
