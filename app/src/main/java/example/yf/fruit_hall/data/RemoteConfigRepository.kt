package example.yf.fruit_hall.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import example.yf.fruit_hall.data.discord.DiscordWebhookTarget
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val json: Json
) {
    suspend fun fetchAndActivate(force: Boolean = false) {
        if (force) {
            remoteConfig.fetch(0L).await()
            remoteConfig.activate().await()
        } else {
            remoteConfig.fetchAndActivate().await()
        }
    }

    fun getScheduleJson(monthKey: String): String? =
        remoteConfig.getString(monthKey).ifEmpty { null }

    fun getDiscordWebhookTargets(): List<DiscordWebhookTarget> =
        remoteConfig.getString("discord_hook_urls")
            .ifEmpty { null }
            ?.let { runCatching { json.decodeFromString<List<DiscordWebhookTarget>>(it) }.getOrNull() }
            .orEmpty()
}
