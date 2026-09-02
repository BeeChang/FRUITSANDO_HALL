package example.yf.fruit_hall.data.discord.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 문구 여러 개를 순서대로 묶어둔 프리셋. itemsJson은 문구 텍스트 리스트를 JSON 배열로 저장한다.
@Entity(tableName = "discord_phrase_presets")
data class DiscordPhrasePresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val itemsJson: String,
    val sortOrder: Int = 0
)
