package example.yf.fruit_hall.data.tray.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import example.yf.fruit_hall.data.tray.entity.AllocationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllocationHistoryDao {
    @Query("SELECT * FROM allocation_history ORDER BY confirmedAt DESC LIMIT 30")
    fun observeRecent(): Flow<List<AllocationHistoryEntity>>

    @Insert
    suspend fun insert(history: AllocationHistoryEntity): Long
}
