package example.yf.fruit_hall.data.rotation.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.rotation.entity.ShiftPresetEntity
import example.yf.fruit_hall.data.rotation.entity.ShiftRoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftPresetDao {
    @Query("SELECT * FROM shift_presets ORDER BY id")
    fun observePresets(): Flow<List<ShiftPresetEntity>>

    @Query("SELECT * FROM shift_roles ORDER BY sortOrder, id")
    fun observeRoles(): Flow<List<ShiftRoleEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun upsertPreset(preset: ShiftPresetEntity): Long

    @Delete
    suspend fun deletePreset(preset: ShiftPresetEntity)

    @Insert(onConflict = REPLACE)
    suspend fun upsertRole(role: ShiftRoleEntity): Long

    @Delete
    suspend fun deleteRole(role: ShiftRoleEntity)

    @Query("UPDATE shift_presets SET isDefault = 0")
    suspend fun clearDefault()

    @Query("UPDATE shift_presets SET isDefault = 1 WHERE id = :id")
    suspend fun setDefault(id: Long)
}
