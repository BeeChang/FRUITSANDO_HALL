package example.yf.fruit_hall.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClockPreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDateFontScale(): Float = prefs.getFloat(KEY_DATE_FONT_SCALE, DEFAULT_FONT_SCALE)

    fun saveDateFontScale(scale: Float) = prefs.edit().putFloat(KEY_DATE_FONT_SCALE, scale).apply()

    fun getTimeFontScale(): Float = prefs.getFloat(KEY_TIME_FONT_SCALE, DEFAULT_FONT_SCALE)

    fun saveTimeFontScale(scale: Float) = prefs.edit().putFloat(KEY_TIME_FONT_SCALE, scale).apply()

    companion object {
        private const val PREFS_NAME          = "clock_prefs"
        private const val KEY_DATE_FONT_SCALE = "date_font_scale"
        private const val KEY_TIME_FONT_SCALE = "time_font_scale"
        const val DEFAULT_FONT_SCALE          = 1.0f
    }
}
