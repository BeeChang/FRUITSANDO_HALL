package example.yf.fruit_hall.data.pos

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PosPreferenceRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("pos_prefs", Context.MODE_PRIVATE)

    var isInitialized: Boolean
        get() = prefs.getBoolean(KEY_INITIALIZED, false)
        set(value) { prefs.edit().putBoolean(KEY_INITIALIZED, value).apply() }

    companion object {
        private const val KEY_INITIALIZED = "initialized"
    }
}
