package example.yf.fruit_hall.data.rotation.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.rotation.entity.RotationPositionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RotationPositionDao {
    @Query("SELECT * FROM rotation_positions ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<RotationPositionEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun upsert(position: RotationPositionEntity): Long

    @Delete
    suspend fun delete(position: RotationPositionEntity)

    @Query("UPDATE rotation_positions SET isActive = :isActive WHERE id = :id")
    suspend fun updateActive(id: Long, isActive: Boolean)
}
