package example.yf.fruit_hall.data.tray.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.tray.entity.SpaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {
    @Query("SELECT * FROM tray_spaces ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<SpaceEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insert(space: SpaceEntity): Long

    @Delete
    suspend fun delete(space: SpaceEntity)

    @Query("UPDATE tray_spaces SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)

    @Query("UPDATE tray_spaces SET capacity = :capacity WHERE id = :id")
    suspend fun updateCapacity(id: Long, capacity: Int?)

    @Query("SELECT COUNT(*) FROM tray_spaces")
    suspend fun count(): Int
}
