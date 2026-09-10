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

    // §10-3 "탭 → 포지션 직접 선택 다이얼로그" — 강제로 포지션(또는 휴게)을 넣을 때 상태까지 같이 바꾼다.
    @Query(
        "UPDATE rotation_cells SET positionId = :positionId, cellState = :cellState, isManuallyEdited = 1 " +
            "WHERE planId = :planId AND slotIndex = :slotIndex AND memberId = :memberId"
    )
    suspend fun updateCellPositionAndState(planId: Long, slotIndex: Int, memberId: Long, positionId: Long?, cellState: String)

    @Query(
        "UPDATE rotation_cells SET isPinned = :isPinned " +
            "WHERE planId = :planId AND slotIndex = :slotIndex AND memberId = :memberId"
    )
    suspend fun updatePinned(planId: Long, slotIndex: Int, memberId: Long, isPinned: Boolean)

    @Query("DELETE FROM rotation_plans WHERE dayPlanId = :dayPlanId")
    suspend fun deletePlansForDayPlan(dayPlanId: Long)
}
