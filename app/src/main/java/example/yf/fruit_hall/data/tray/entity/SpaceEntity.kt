package example.yf.fruit_hall.data.tray.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tray_spaces")
data class SpaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0
)
