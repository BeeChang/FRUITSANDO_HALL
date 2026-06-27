package example.yf.fruit_hall.data.position.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import example.yf.fruit_hall.data.position.entity.AssignmentEntity

@Dao
interface AssignmentDao {

    @Query("SELECT * FROM assignments WHERE date = :date ORDER BY slotNumber")
    suspend fun getByDate(date: String): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE date = :date AND slotNumber = :slot")
    suspend fun getByDateAndSlot(date: String, slot: Int): List<AssignmentEntity>

    @Insert
    suspend fun insertAll(assignments: List<AssignmentEntity>)

    @Query("DELETE FROM assignments WHERE date = :date AND slotNumber = :slot")
    suspend fun deleteByDateAndSlot(date: String, slot: Int)

    @Query("DELETE FROM assignments WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM assignments")
    suspend fun deleteAll()

    @Query("SELECT * FROM assignments ORDER BY date DESC, slotNumber DESC")
    suspend fun getAll(): List<AssignmentEntity>
}
