package example.yf.fruit_hall.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ClockAlert(
    val id: String,
    val hour: Int,
    val minute: Int,
    val message: String,
    val enabled: Boolean = true
)

@Singleton
class ClockPreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun getDateFontScale(): Float = prefs.getFloat(KEY_DATE_FONT_SCALE, DEFAULT_FONT_SCALE)

    fun saveDateFontScale(scale: Float) = prefs.edit().putFloat(KEY_DATE_FONT_SCALE, scale).apply()

    fun getTimeFontScale(): Float = prefs.getFloat(KEY_TIME_FONT_SCALE, DEFAULT_FONT_SCALE)

    fun saveTimeFontScale(scale: Float) = prefs.edit().putFloat(KEY_TIME_FONT_SCALE, scale).apply()

    fun getFlashDurationSeconds(): Int = prefs.getInt(KEY_FLASH_DURATION, DEFAULT_FLASH_DURATION_SECONDS)

    fun saveFlashDurationSeconds(seconds: Int) = prefs.edit().putInt(KEY_FLASH_DURATION, seconds).apply()

    fun getAlerts(): List<ClockAlert> {
        val raw = prefs.getString(KEY_ALERTS, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<ClockAlert>>(raw) }.getOrDefault(emptyList())
    }

    fun saveAlerts(alerts: List<ClockAlert>) =
        prefs.edit().putString(KEY_ALERTS, json.encodeToString(alerts)).apply()

    companion object {
        private const val PREFS_NAME            = "clock_prefs"
        private const val KEY_DATE_FONT_SCALE   = "date_font_scale"
        private const val KEY_TIME_FONT_SCALE   = "time_font_scale"
        private const val KEY_FLASH_DURATION    = "flash_duration_seconds"
        private const val KEY_ALERTS            = "alerts"
        const val DEFAULT_FONT_SCALE             = 1.0f
        const val DEFAULT_FLASH_DURATION_SECONDS = 10
    }
}
