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

@Serializable
private data class DiscordMessageResponse(val id: String)

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

    /** 전송 성공 시 디스코드 메시지 id를 돌려준다 (?wait=true로 응답 본문을 받아온다) — 이후 수정·삭제 요청에 필요. */
    suspend fun sendMessage(content: String, webhookUrl: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val body = json.encodeToString(DiscordWebhookPayload.serializer(), DiscordWebhookPayload(content))
                .toRequestBody("application/json".toMediaType())
            val waitUrl = webhookUrl + if (webhookUrl.contains("?")) "&wait=true" else "?wait=true"
            val request = Request.Builder()
                .url(waitUrl)
                .post(body)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
                val responseBody = response.body?.string().orEmpty()
                json.decodeFromString(DiscordMessageResponse.serializer(), responseBody).id
            }
        }
    }

    suspend fun editMessage(webhookUrl: String, messageId: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val body = json.encodeToString(DiscordWebhookPayload.serializer(), DiscordWebhookPayload(content))
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("${trimWebhookUrl(webhookUrl)}/messages/$messageId")
                .patch(body)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
            }
        }
    }

    suspend fun deleteMessage(webhookUrl: String, messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("${trimWebhookUrl(webhookUrl)}/messages/$messageId")
                .delete()
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
            }
        }
    }

    private fun trimWebhookUrl(webhookUrl: String): String = webhookUrl.substringBefore("?")
}
