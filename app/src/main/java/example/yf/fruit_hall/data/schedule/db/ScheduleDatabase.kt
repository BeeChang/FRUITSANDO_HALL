package example.yf.fruit_hall.data.schedule.db

import androidx.room.Database
import androidx.room.RoomDatabase
import example.yf.fruit_hall.data.schedule.dao.ScheduleDao
import example.yf.fruit_hall.data.schedule.entity.ScheduleEntryEntity
import example.yf.fruit_hall.data.schedule.entity.ScheduleMetadataEntity

@Database(
    entities = [ScheduleMetadataEntity::class, ScheduleEntryEntity::class],
    version = 1
)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
}
