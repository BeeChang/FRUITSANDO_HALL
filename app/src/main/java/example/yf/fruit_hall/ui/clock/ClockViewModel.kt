package example.yf.fruit_hall.ui.clock

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.data.ClockAlert
import example.yf.fruit_hall.data.ClockPreferenceRepository
import example.yf.fruit_hall.data.ExternalAppRepository
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    private val externalAppRepo: ExternalAppRepository,
    private val clockPreferenceRepo: ClockPreferenceRepository
) : ViewModel() {

    var packageName by mutableStateOf(externalAppRepo.getPackageName())
        private set
    var isLocked by mutableStateOf(externalAppRepo.isLocked())
        private set

    var dateFontScale by mutableFloatStateOf(clockPreferenceRepo.getDateFontScale())
        private set
    var timeFontScale by mutableFloatStateOf(clockPreferenceRepo.getTimeFontScale())
        private set

    var flashDurationSeconds by mutableIntStateOf(clockPreferenceRepo.getFlashDurationSeconds())
        private set

    var alerts by mutableStateOf(clockPreferenceRepo.getAlerts())
        private set

    fun addAlert(hour: Int, minute: Int, message: String) {
        alerts = alerts + ClockAlert(id = UUID.randomUUID().toString(), hour = hour, minute = minute, message = message)
        clockPreferenceRepo.saveAlerts(alerts)
    }

    fun removeAlert(id: String) {
        alerts = alerts.filterNot { it.id == id }
        clockPreferenceRepo.saveAlerts(alerts)
    }

    fun toggleAlert(id: String, enabled: Boolean) {
        alerts = alerts.map { if (it.id == id) it.copy(enabled = enabled) else it }
        clockPreferenceRepo.saveAlerts(alerts)
    }

    fun increaseFlashDuration() {
        flashDurationSeconds = (flashDurationSeconds + FLASH_DURATION_STEP).coerceAtMost(MAX_FLASH_DURATION_SECONDS)
        clockPreferenceRepo.saveFlashDurationSeconds(flashDurationSeconds)
    }

    fun decreaseFlashDuration() {
        flashDurationSeconds = (flashDurationSeconds - FLASH_DURATION_STEP).coerceAtLeast(MIN_FLASH_DURATION_SECONDS)
        clockPreferenceRepo.saveFlashDurationSeconds(flashDurationSeconds)
    }

    fun saveSettings(pkg: String, locked: Boolean) {
        externalAppRepo.savePackageName(pkg)
        packageName = pkg
        externalAppRepo.saveLocked(locked)
        isLocked = locked
    }

    fun increaseDateFontScale() {
        dateFontScale = (dateFontScale + FONT_SCALE_STEP).coerceAtMost(MAX_FONT_SCALE)
        clockPreferenceRepo.saveDateFontScale(dateFontScale)
    }

    fun decreaseDateFontScale() {
        dateFontScale = (dateFontScale - FONT_SCALE_STEP).coerceAtLeast(MIN_FONT_SCALE)
        clockPreferenceRepo.saveDateFontScale(dateFontScale)
    }

    fun increaseTimeFontScale() {
        timeFontScale = (timeFontScale + FONT_SCALE_STEP).coerceAtMost(MAX_FONT_SCALE)
        clockPreferenceRepo.saveTimeFontScale(timeFontScale)
    }

    fun decreaseTimeFontScale() {
        timeFontScale = (timeFontScale - FONT_SCALE_STEP).coerceAtLeast(MIN_FONT_SCALE)
        clockPreferenceRepo.saveTimeFontScale(timeFontScale)
    }

    companion object {
        const val MIN_FONT_SCALE  = 0.6f
        const val MAX_FONT_SCALE  = 1.8f
        const val FONT_SCALE_STEP = 0.1f

        const val MIN_FLASH_DURATION_SECONDS = 5
        const val MAX_FLASH_DURATION_SECONDS = 30
        const val FLASH_DURATION_STEP        = 5
    }
}
