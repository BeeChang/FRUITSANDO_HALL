package example.yf.fruit_hall.data.discord.local

import androidx.room.Database
import androidx.room.RoomDatabase
import example.yf.fruit_hall.data.discord.local.dao.DiscordPhraseDao
import example.yf.fruit_hall.data.discord.local.dao.DiscordPhrasePresetDao
import example.yf.fruit_hall.data.discord.local.dao.DiscordSentMessageDao
import example.yf.fruit_hall.data.discord.local.dao.DiscordTagDao
import example.yf.fruit_hall.data.discord.local.entity.DiscordPhraseEntity
import example.yf.fruit_hall.data.discord.local.entity.DiscordPhrasePresetEntity
import example.yf.fruit_hall.data.discord.local.entity.DiscordSentMessageEntity
import example.yf.fruit_hall.data.discord.local.entity.DiscordTagEntity

@Database(
    entities = [
        DiscordTagEntity::class, DiscordPhraseEntity::class, DiscordSentMessageEntity::class,
        DiscordPhrasePresetEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DiscordContentDatabase : RoomDatabase() {
    abstract fun tagDao(): DiscordTagDao
    abstract fun phraseDao(): DiscordPhraseDao
    abstract fun sentMessageDao(): DiscordSentMessageDao
    abstract fun phrasePresetDao(): DiscordPhrasePresetDao
}
