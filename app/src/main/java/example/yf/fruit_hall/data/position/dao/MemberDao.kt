package example.yf.fruit_hall.data.position.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import example.yf.fruit_hall.data.position.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Query("SELECT * FROM members ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<MemberEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Query("UPDATE members SET isDeleted = 1, isWorking = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("UPDATE members SET isWorking = :isWorking WHERE id = :id")
    suspend fun setWorking(id: Long, isWorking: Boolean)

    @Query("UPDATE members SET isWorking = 0")
    suspend fun resetAllWorking()

    @Query("UPDATE members SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @androidx.room.Transaction
    suspend fun updateSortOrders(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> updateSortOrder(id, index) }
    }

    @Query("SELECT COUNT(*) FROM members")
    suspend fun count(): Int
}
