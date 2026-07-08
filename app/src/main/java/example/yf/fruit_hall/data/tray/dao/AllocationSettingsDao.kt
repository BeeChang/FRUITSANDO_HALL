package example.yf.fruit_hall.data.tray.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Upsert
import example.yf.fruit_hall.data.tray.entity.AllocationSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllocationSettingsDao {
    @Query("SELECT * FROM allocation_settings WHERE id = 1")
    fun observe(): Flow<AllocationSettingsEntity?>

    @Upsert
    suspend fun upsert(settings: AllocationSettingsEntity)
}
