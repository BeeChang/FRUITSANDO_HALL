package example.yf.fruit_hall.data.position.db

import androidx.room.Database
import androidx.room.RoomDatabase
import example.yf.fruit_hall.data.position.dao.AssignmentDao
import example.yf.fruit_hall.data.position.dao.MemberDao
import example.yf.fruit_hall.data.position.dao.PositionDao
import example.yf.fruit_hall.data.position.dao.SlotSettingsDao
import example.yf.fruit_hall.data.position.dao.WorkDayDao
import example.yf.fruit_hall.data.position.entity.AssignmentEntity
import example.yf.fruit_hall.data.position.entity.MemberEntity
import example.yf.fruit_hall.data.position.entity.PositionEntity
import example.yf.fruit_hall.data.position.entity.SlotSettingsEntity
import example.yf.fruit_hall.data.position.entity.WorkDayEntity

@Database(
    entities = [
        MemberEntity::class,
        PositionEntity::class,
        WorkDayEntity::class,
        SlotSettingsEntity::class,
        AssignmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PositionDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun positionDao(): PositionDao
    abstract fun workDayDao(): WorkDayDao
    abstract fun slotSettingsDao(): SlotSettingsDao
    abstract fun assignmentDao(): AssignmentDao
}
