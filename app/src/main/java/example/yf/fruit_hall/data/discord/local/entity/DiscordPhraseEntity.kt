package example.yf.fruit_hall.data.discord.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 자주 쓰는 문장을 저장해두고 메시지 조합 목록에 끌어다 쓴다.
@Entity(tableName = "discord_phrases")
data class DiscordPhraseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val sortOrder: Int = 0
)
