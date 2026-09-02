package example.yf.fruit_hall.data.rotation.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.rotation.entity.ScheduleTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleTemplateDao {
    @Query("SELECT * FROM schedule_templates ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<ScheduleTemplateEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun upsert(template: ScheduleTemplateEntity): Long

    @Delete
    suspend fun delete(template: ScheduleTemplateEntity)
}
