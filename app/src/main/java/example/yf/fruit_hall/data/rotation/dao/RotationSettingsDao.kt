package example.yf.fruit_hall.data.rotation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.rotation.entity.RotationSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RotationSettingsDao {
    @Query("SELECT * FROM rotation_settings WHERE id = 1")
    fun observe(): Flow<RotationSettingsEntity?>

    @Insert(onConflict = REPLACE)
    suspend fun upsert(settings: RotationSettingsEntity)
}
