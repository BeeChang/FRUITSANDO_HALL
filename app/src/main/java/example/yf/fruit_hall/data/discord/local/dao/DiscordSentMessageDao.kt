package example.yf.fruit_hall.data.discord.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import example.yf.fruit_hall.data.discord.local.entity.DiscordSentMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscordSentMessageDao {
    @Query("SELECT * FROM discord_sent_messages ORDER BY sentAt DESC LIMIT 20")
    fun observeRecent(): Flow<List<DiscordSentMessageEntity>>

    @Insert
    suspend fun insert(message: DiscordSentMessageEntity): Long

    @Update
    suspend fun update(message: DiscordSentMessageEntity)

    @Delete
    suspend fun delete(message: DiscordSentMessageEntity)
}
