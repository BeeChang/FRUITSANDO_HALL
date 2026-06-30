package example.yf.fruit_hall.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.data.NavigationPreferenceRepository
import example.yf.fruit_hall.data.schedule.ScheduleRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val navPrefs: NavigationPreferenceRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    init {
        syncSchedule()
    }

    var isRailVisible by mutableStateOf(true)
        private set

    val initialRoute: MainRoute = when (navPrefs.loadLastRoute()) {
        NavigationPreferenceRepository.KEY_BEOMURI -> MainRoute.Beomuri
        NavigationPreferenceRepository.KEY_POSITION -> MainRoute.Position
        NavigationPreferenceRepository.KEY_CLOCK -> MainRoute.Clock
        NavigationPreferenceRepository.KEY_SCHEDULE -> MainRoute.Schedule
        else -> MainRoute.Pos
    }

    // Activity 재생성(화면 회전) 시에도 ViewModel은 살아남으므로 이 플래그는 유지된다.
    // LaunchedEffect는 새 Composition 진입 시 항상 실행되므로, 이 플래그로 최초 1회만 navigate하도록 막는다.
    var hasNavigatedInitially = false
        private set

    fun markNavigatedInitially() {
        hasNavigatedInitially = true
    }

    fun toggleRail() {
        isRailVisible = !isRailVisible
    }

    private fun syncSchedule() {
        viewModelScope.launch {
            val fmt = DateTimeFormatter.ofPattern("yyyyMM")
            val today = LocalDate.now()
            listOf(today, today.plusMonths(1), today.plusMonths(2)).forEach { date ->
                runCatching { scheduleRepository.syncIfNeeded("s" + date.format(fmt)) }
            }
        }
    }

    fun onRouteSelected(route: MainRoute) {
        val key = when (route) {
            MainRoute.Pos -> NavigationPreferenceRepository.KEY_POS
            MainRoute.Beomuri -> NavigationPreferenceRepository.KEY_BEOMURI
            MainRoute.Position -> NavigationPreferenceRepository.KEY_POSITION
            MainRoute.Clock -> NavigationPreferenceRepository.KEY_CLOCK
            MainRoute.Schedule -> NavigationPreferenceRepository.KEY_SCHEDULE
        }
        navPrefs.saveLastRoute(key)
    }
}
