package example.yf.fruit_hall.data.position.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WeightDecayMode { STAY_LOW, RECOVER }

@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isMultiPerson: Boolean = false,
    val hasWeight: Boolean = false,
    val weightStrength: Float = 0.8f,
    val weightDecayMode: String = WeightDecayMode.STAY_LOW.name,
    val sortOrder: Int = 0,
    val isDeleted: Boolean = false
)
