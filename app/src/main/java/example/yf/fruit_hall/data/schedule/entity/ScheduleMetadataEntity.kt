package example.yf.fruit_hall.data.schedule.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_metadata")
data class ScheduleMetadataEntity(
    @PrimaryKey val monthKey: String,
    val updated: String,
    val syncedAt: Long
)
