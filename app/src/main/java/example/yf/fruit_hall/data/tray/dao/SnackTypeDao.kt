package example.yf.fruit_hall.data.tray.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.tray.entity.SnackTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SnackTypeDao {
    @Query("SELECT * FROM snack_types ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<SnackTypeEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insert(snackType: SnackTypeEntity): Long

    @Delete
    suspend fun delete(snackType: SnackTypeEntity)

    @Query("UPDATE snack_types SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("UPDATE snack_types SET isActive = :isActive WHERE id = :id")
    suspend fun updateActive(id: Long, isActive: Boolean)

    @androidx.room.Transaction
    suspend fun updateSortOrders(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> updateSortOrder(id, index) }
    }

    @Query("SELECT COUNT(*) FROM snack_types")
    suspend fun count(): Int
}
