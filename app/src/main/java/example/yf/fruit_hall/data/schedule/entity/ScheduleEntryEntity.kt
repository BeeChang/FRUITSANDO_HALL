package example.yf.fruit_hall.data.schedule.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_entry",
    indices = [Index(value = ["monthKey", "date", "personName"], unique = true)]
)
data class ScheduleEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthKey: String,
    val date: String,
    val personName: String,
    val shift: String
)
