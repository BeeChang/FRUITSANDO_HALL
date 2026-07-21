package example.yf.fruit_hall.data.tray.dao

import androidx.room.Dao
import androidx.room.Insert
import example.yf.fruit_hall.data.tray.entity.ManualEditLogEntity

@Dao
interface ManualEditLogDao {
    @Insert
    suspend fun insert(log: ManualEditLogEntity): Long
}
