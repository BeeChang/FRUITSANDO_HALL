package example.yf.fruit_hall.data.position.dao

import androidx.room.Dao
import androidx.room.Delete
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

    @Delete
    suspend fun delete(member: MemberEntity)

    @Query("UPDATE members SET isWorking = :isWorking WHERE id = :id")
    suspend fun setWorking(id: Long, isWorking: Boolean)

    @Query("SELECT COUNT(*) FROM members")
    suspend fun count(): Int
}
