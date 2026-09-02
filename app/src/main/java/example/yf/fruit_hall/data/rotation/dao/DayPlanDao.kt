package example.yf.fruit_hall.data.rotation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.rotation.entity.BreakAssignmentEntity
import example.yf.fruit_hall.data.rotation.entity.DayPlanEntity
import example.yf.fruit_hall.data.rotation.entity.DayRoleAssignmentEntity

@Dao
interface DayPlanDao {
    @Query("SELECT * FROM day_plans WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DayPlanEntity?

    @Insert(onConflict = REPLACE)
    suspend fun upsertDayPlan(dayPlan: DayPlanEntity): Long

    @Query("SELECT * FROM day_role_assignments WHERE dayPlanId = :dayPlanId ORDER BY id")
    suspend fun getRoleAssignments(dayPlanId: Long): List<DayRoleAssignmentEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insertRoleAssignments(assignments: List<DayRoleAssignmentEntity>)

    @Query("DELETE FROM day_role_assignments WHERE dayPlanId = :dayPlanId")
    suspend fun deleteRoleAssignments(dayPlanId: Long)

    @Query("SELECT * FROM break_assignments WHERE dayPlanId = :dayPlanId ORDER BY id")
    suspend fun getBreaks(dayPlanId: Long): List<BreakAssignmentEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insertBreaks(breaks: List<BreakAssignmentEntity>)

    @Query("DELETE FROM break_assignments WHERE dayPlanId = :dayPlanId")
    suspend fun deleteBreaks(dayPlanId: Long)
}
