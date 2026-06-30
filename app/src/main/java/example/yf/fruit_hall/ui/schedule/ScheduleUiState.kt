package example.yf.fruit_hall.ui.schedule

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val displayMonth: String = "",
    val currentMonthKey: String = "",
    val isCurrentMonth: Boolean = false,
    val weeks: List<List<CalendarDay?>> = emptyList(),
    val todayWeekIndex: Int = -1,
    val hasData: Boolean = false
)

data class CalendarDay(
    val dayOfMonth: Int,
    val date: String,
    val isToday: Boolean,
    val isWeekend: Boolean,
    val isTrailing: Boolean,
    val isPast: Boolean,
    val shifts: List<ShiftEntry>
)

data class ShiftEntry(
    val personName: String,
    val shift: String
)
