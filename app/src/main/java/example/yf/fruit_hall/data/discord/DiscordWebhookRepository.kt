package example.yf.fruit_hall.data.discord

import example.yf.fruit_hall.data.RemoteConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class DiscordWebhookPayload(val content: String)

@Singleton
class DiscordWebhookRepository @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    suspend fun fetchWebhookTargets(): List<DiscordWebhookTarget> = withContext(Dispatchers.IO) {
        remoteConfigRepository.fetchAndActivate()
        remoteConfigRepository.getDiscordWebhookTargets()
    }

    suspend fun sendMessage(content: String, webhookUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val body = json.encodeToString(DiscordWebhookPayload.serializer(), DiscordWebhookPayload(content))
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
            }
        }
    }
}
