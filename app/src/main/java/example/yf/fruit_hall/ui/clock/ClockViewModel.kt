package example.yf.fruit_hall.ui.clock

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.data.ExternalAppRepository
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    private val externalAppRepo: ExternalAppRepository
) : ViewModel() {

    var packageName by mutableStateOf(externalAppRepo.getPackageName())
        private set
    var isLocked by mutableStateOf(externalAppRepo.isLocked())
        private set

    fun saveSettings(pkg: String, locked: Boolean) {
        externalAppRepo.savePackageName(pkg)
        packageName = pkg
        externalAppRepo.saveLocked(locked)
        isLocked = locked
    }
}
