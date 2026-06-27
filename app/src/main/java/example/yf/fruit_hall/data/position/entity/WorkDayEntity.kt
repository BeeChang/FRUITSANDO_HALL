package example.yf.fruit_hall.data.position.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_days")
data class WorkDayEntity(
    @PrimaryKey val date: String,
    val currentSlot: Int = 1
)