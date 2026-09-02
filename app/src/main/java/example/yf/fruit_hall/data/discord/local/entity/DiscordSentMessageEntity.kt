package example.yf.fruit_hall.data.discord.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 전송된 디스코드 메시지 기록. 디스코드가 돌려준 messageId를 들고 있어야 이후 수정·삭제 요청을 보낼 수 있다.
@Entity(tableName = "discord_sent_messages")
data class DiscordSentMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: String,
    val webhookUrl: String,
    val webhookName: String,
    val content: String,
    val sentAt: Long
)
