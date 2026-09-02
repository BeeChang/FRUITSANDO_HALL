package example.yf.fruit_hall.data.discord.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 디스코드 멘션/역할 호출자(예: @here, @everyone)를 저장해두고 메시지에 끌어다 쓴다.
@Entity(tableName = "discord_tags")
data class DiscordTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val sortOrder: Int = 0
)
