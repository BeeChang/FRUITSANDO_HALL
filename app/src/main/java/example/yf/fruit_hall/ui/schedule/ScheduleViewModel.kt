package example.yf.fruit_hall.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.data.schedule.ScheduleRepository
import example.yf.fruit_hall.data.schedule.entity.ScheduleEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val monthFmt = DateTimeFormatter.ofPattern("yyyyMM")
    private val dateFmt  = DateTimeFormatter.ofPattern("yyyyMMdd")

    private val _monthKey  = MutableStateFlow(todayMonthKey())
    private val _isSyncing = MutableStateFlow(false)

    val uiState = combine(
        _monthKey.flatMapLatest { scheduleRepository.observeEntriesForMonth(it) },
        _monthKey.flatMapLatest { scheduleRepository.observeEntriesForMonth(nextMonthKey(it)) },
        _monthKey,
        _isSyncing
    ) { currentEntries, nextEntries, monthKey, isSyncing ->
        buildUiState(monthKey, currentEntries, nextEntries, isSyncing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduleUiState())

    init { sync() }

    fun onPrevMonth() = _monthKey.update { prevMonthKey(it) }
    fun onNextMonth() = _monthKey.update { nextMonthKey(it) }
    fun onRefresh()   = sync()

    private fun sync() {
        viewModelScope.launch {
            _isSyncing.value = true
            val today = LocalDate.now()
            listOf(today, today.plusMonths(1), today.plusMonths(2)).forEach { date ->
                runCatching { scheduleRepository.syncIfNeeded("s" + date.format(monthFmt)) }
            }
            _isSyncing.value = false
        }
    }

    private fun buildUiState(
        monthKey: String,
        currentEntries: List<ScheduleEntryEntity>,
        nextEntries: List<ScheduleEntryEntity>,
        isSyncing: Boolean
    ): ScheduleUiState {
        val ym    = YearMonth.parse(monthKey.substring(1), monthFmt)
        val nextYm = ym.plusMonths(1)
        val today  = LocalDate.now()
        val isCurrentMonth = ym.year == today.year && ym.monthValue == today.monthValue

        val currentShiftMap = currentEntries.filter { it.shift != "off" }.groupBy { it.date }
        val nextShiftMap    = nextEntries.filter { it.shift != "off" }.groupBy { it.date }

        val startOffset = (ym.atDay(1).dayOfWeek.value - 1) % 7
        val cells = mutableListOf<CalendarDay?>()
        repeat(startOffset) { cells.add(null) }

        for (day in 1..ym.lengthOfMonth()) {
            val date    = ym.atDay(day)
            val dateStr = date.format(dateFmt)
            cells.add(
                CalendarDay(
                    dayOfMonth = day,
                    date       = dateStr,
                    isToday    = date == today,
                    isWeekend  = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY,
                    isTrailing = false,
                    isPast     = isCurrentMonth && date.isBefore(today),
                    shifts     = currentShiftMap[dateStr]?.map { ShiftEntry(it.personName, it.shift) } ?: emptyList()
                )
            )
        }

        val trailingCount = if (cells.size % 7 == 0) 0 else 7 - (cells.size % 7)
        for (day in 1..trailingCount) {
            val date    = nextYm.atDay(day)
            val dateStr = date.format(dateFmt)
            cells.add(
                CalendarDay(
                    dayOfMonth = day,
                    date       = dateStr,
                    isToday    = date == today,
                    isWeekend  = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY,
                    isTrailing = true,
                    isPast     = false,
                    shifts     = nextShiftMap[dateStr]?.map { ShiftEntry(it.personName, it.shift) } ?: emptyList()
                )
            )
        }

        val weeks = cells.chunked(7)
        return ScheduleUiState(
            isLoading       = false,
            isSyncing       = isSyncing,
            displayMonth    = displayMonth(monthKey),
            currentMonthKey = monthKey,
            isCurrentMonth  = isCurrentMonth,
            weeks           = weeks,
            todayWeekIndex  = weeks.indexOfFirst { w -> w.any { it?.isToday == true } },
            hasData         = currentEntries.isNotEmpty()
        )
    }

    private fun todayMonthKey()            = "s" + LocalDate.now().format(monthFmt)
    private fun prevMonthKey(key: String)  = "s" + YearMonth.parse(key.substring(1), monthFmt).minusMonths(1).format(monthFmt)
    private fun nextMonthKey(key: String)  = "s" + YearMonth.parse(key.substring(1), monthFmt).plusMonths(1).format(monthFmt)
    private fun displayMonth(key: String): String {
        val ym = YearMonth.parse(key.substring(1), monthFmt)
        return "${ym.year}년 ${ym.monthValue}월"
    }
}
