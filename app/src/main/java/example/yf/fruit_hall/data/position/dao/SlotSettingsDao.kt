package example.yf.fruit_hall.data.position.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.position.entity.SlotSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SlotSettingsDao {

    @Query("SELECT * FROM slot_settings WHERE id = 1")
    fun observe(): Flow<SlotSettingsEntity?>

    @Insert(onConflict = REPLACE)
    suspend fun upsert(settings: SlotSettingsEntity)
}
