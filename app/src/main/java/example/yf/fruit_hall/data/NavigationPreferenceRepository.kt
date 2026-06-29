package example.yf.fruit_hall.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationPreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLastRoute(key: String) = prefs.edit().putString(KEY_LAST_ROUTE, key).apply()

    fun loadLastRoute(): String = prefs.getString(KEY_LAST_ROUTE, KEY_POS) ?: KEY_POS

    companion object {
        private const val PREFS_NAME = "navigation_prefs"
        private const val KEY_LAST_ROUTE = "last_route"
        const val KEY_POS = "Pos"
        const val KEY_BEOMURI = "Beomuri"
        const val KEY_POSITION = "Position"
    }
}
