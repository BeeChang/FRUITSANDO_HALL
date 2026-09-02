package example.yf.fruit_hall.data.discord

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscordPreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastWebhookName: String
        get() = prefs.getString(KEY_LAST_WEBHOOK, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_WEBHOOK, value).apply()

    companion object {
        private const val PREFS_NAME = "discord_prefs"
        private const val KEY_LAST_WEBHOOK = "last_webhook_name"
    }
}
