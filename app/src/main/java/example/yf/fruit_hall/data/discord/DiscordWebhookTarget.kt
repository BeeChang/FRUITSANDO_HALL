package example.yf.fruit_hall.data.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordWebhookTarget(val name: String, val url: String)
