package example.yf.fruit_hall.data.discord.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.discord.local.entity.DiscordPhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscordPhraseDao {
    @Query("SELECT * FROM discord_phrases ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<DiscordPhraseEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun upsert(phrase: DiscordPhraseEntity): Long

    @Delete
    suspend fun delete(phrase: DiscordPhraseEntity)
}
