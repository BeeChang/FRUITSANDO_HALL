package example.yf.fruit_hall.data.position.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slot_settings")
data class SlotSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val totalSlots: Int = 3
)