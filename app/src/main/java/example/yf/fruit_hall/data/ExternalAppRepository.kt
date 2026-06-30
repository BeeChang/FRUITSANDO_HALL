package example.yf.fruit_hall.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalAppRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPackageName(): String =
        prefs.getString(KEY_PACKAGE, DEFAULT_PACKAGE) ?: DEFAULT_PACKAGE

    fun savePackageName(pkg: String) =
        prefs.edit().putString(KEY_PACKAGE, pkg).apply()

    fun isLocked(): Boolean = prefs.getBoolean(KEY_LOCKED, false)

    fun saveLocked(locked: Boolean) =
        prefs.edit().putBoolean(KEY_LOCKED, locked).apply()

    companion object {
        private const val PREFS_NAME    = "external_app_prefs"
        private const val KEY_PACKAGE   = "package_name"
        private const val KEY_LOCKED    = "is_locked"
        const val DEFAULT_PACKAGE       = "co.kr.catchtable.waiting"
    }
}
