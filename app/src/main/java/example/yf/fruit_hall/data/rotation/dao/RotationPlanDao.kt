package example.yf.fruit_hall.data.rotation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.rotation.entity.RotationCellEntity
import example.yf.fruit_hall.data.rotation.entity.RotationPlanEntity
import example.yf.fruit_hall.data.rotation.entity.RotationSlotEntity

@Dao
interface RotationPlanDao {
    @Query("SELECT * FROM rotation_plans WHERE dayPlanId = :dayPlanId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestForDayPlan(dayPlanId: Long): RotationPlanEntity?

    @Insert(onConflict = REPLACE)
    suspend fun upsertPlan(plan: RotationPlanEntity): Long

    @Query("SELECT * FROM rotation_slots WHERE planId = :planId ORDER BY slotIndex")
    suspend fun getSlots(planId: Long): List<RotationSlotEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insertSlots(slots: List<RotationSlotEntity>)

    @Query("DELETE FROM rotation_slots WHERE planId = :planId")
    suspend fun deleteSlots(planId: Long)

    @Query("SELECT * FROM rotation_cells WHERE planId = :planId ORDER BY slotIndex")
    suspend fun getCells(planId: Long): List<RotationCellEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insertCells(cells: List<RotationCellEntity>)

    @Query("DELETE FROM rotation_cells WHERE planId = :planId")
    suspend fun deleteCells(planId: Long)

    @Query(
        "UPDATE rotation_cells SET positionId = :positionId, isManuallyEdited = 1 " +
            "WHERE planId = :planId AND slotIndex = :slotIndex AND memberId = :memberId"
    )
    suspend fun updateCellPosition(planId: Long, slotIndex: Int, memberId: Long, positionId: Long?)

    @Query(
        "UPDATE rotation_cells SET isPinned = :isPinned " +
            "WHERE planId = :planId AND slotIndex = :slotIndex AND memberId = :memberId"
    )
    suspend fun updatePinned(planId: Long, slotIndex: Int, memberId: Long, isPinned: Boolean)
}
