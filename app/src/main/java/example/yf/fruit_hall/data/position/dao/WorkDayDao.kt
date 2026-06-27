package example.yf.fruit_hall.data.position.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.position.entity.WorkDayEntity

@Dao
interface WorkDayDao {

    @Query("SELECT * FROM work_days WHERE date = :date")
    suspend fun getByDate(date: String): WorkDayEntity?

    @Insert(onConflict = REPLACE)
    suspend fun upsert(day: WorkDayEntity)

    @Query("DELETE FROM work_days WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM work_days")
    suspend fun deleteAll()
}
