package example.yf.fruit_hall.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.data.NavigationPreferenceRepository
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val navPrefs: NavigationPreferenceRepository
) : ViewModel() {

    var isRailVisible by mutableStateOf(true)
        private set

    val initialRoute: MainRoute = when (navPrefs.loadLastRoute()) {
        NavigationPreferenceRepository.KEY_SECOND -> MainRoute.Second
        NavigationPreferenceRepository.KEY_THIRD -> MainRoute.Third
        else -> MainRoute.Home
    }

    fun toggleRail() {
        isRailVisible = !isRailVisible
    }

    fun onRouteSelected(route: MainRoute) {
        val key = when (route) {
            MainRoute.Home -> NavigationPreferenceRepository.KEY_HOME
            MainRoute.Second -> NavigationPreferenceRepository.KEY_SECOND
            MainRoute.Third -> NavigationPreferenceRepository.KEY_THIRD
        }
        navPrefs.saveLastRoute(key)
    }
}