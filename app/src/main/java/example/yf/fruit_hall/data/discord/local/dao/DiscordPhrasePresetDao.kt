package example.yf.fruit_hall.data.discord.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.discord.local.entity.DiscordPhrasePresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscordPhrasePresetDao {
    @Query("SELECT * FROM discord_phrase_presets ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<DiscordPhrasePresetEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun upsert(preset: DiscordPhrasePresetEntity): Long

    @Delete
    suspend fun delete(preset: DiscordPhrasePresetEntity)
}
