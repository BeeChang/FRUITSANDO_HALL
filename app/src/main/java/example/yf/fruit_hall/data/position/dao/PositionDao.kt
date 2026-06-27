package example.yf.fruit_hall.data.position.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import example.yf.fruit_hall.data.position.entity.PositionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionDao {

    @Query("SELECT * FROM positions ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<PositionEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insert(position: PositionEntity): Long

    @Update
    suspend fun update(position: PositionEntity)

    @Delete
    suspend fun delete(position: PositionEntity)

    @Query("SELECT COUNT(*) FROM positions")
    suspend fun count(): Int
}
