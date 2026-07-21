package example.yf.fruit_hall.data.tray.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import example.yf.fruit_hall.data.tray.entity.TrayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrayDao {
    @Query("SELECT * FROM trays ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<TrayEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insert(tray: TrayEntity): Long

    @Update
    suspend fun update(tray: TrayEntity)

    @Delete
    suspend fun delete(tray: TrayEntity)

    @Query("UPDATE trays SET pinnedRound = :pinnedRound WHERE id = :id")
    suspend fun setPinnedRound(id: Long, pinnedRound: Int?)

    @Query("DELETE FROM trays")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM trays")
    suspend fun count(): Int
}
