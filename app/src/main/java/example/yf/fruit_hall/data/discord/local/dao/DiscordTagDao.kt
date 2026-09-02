package example.yf.fruit_hall.data.discord.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.discord.local.entity.DiscordTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscordTagDao {
    @Query("SELECT * FROM discord_tags ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<DiscordTagEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun upsert(tag: DiscordTagEntity): Long

    @Delete
    suspend fun delete(tag: DiscordTagEntity)
}
