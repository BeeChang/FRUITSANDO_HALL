package example.yf.fruit_hall.data.rotation.db

import androidx.room.Database
import androidx.room.RoomDatabase
import example.yf.fruit_hall.data.rotation.dao.DayPlanDao
import example.yf.fruit_hall.data.rotation.dao.RotationPlanDao
import example.yf.fruit_hall.data.rotation.dao.RotationPositionDao
import example.yf.fruit_hall.data.rotation.dao.RotationSettingsDao
import example.yf.fruit_hall.data.rotation.dao.ScheduleTemplateDao
import example.yf.fruit_hall.data.rotation.dao.ShiftPresetDao
import example.yf.fruit_hall.data.rotation.entity.BreakAssignmentEntity
import example.yf.fruit_hall.data.rotation.entity.DayPlanEntity
import example.yf.fruit_hall.data.rotation.entity.DayRoleAssignmentEntity
import example.yf.fruit_hall.data.rotation.entity.RotationCellEntity
import example.yf.fruit_hall.data.rotation.entity.RotationPlanEntity
import example.yf.fruit_hall.data.rotation.entity.RotationPositionEntity
import example.yf.fruit_hall.data.rotation.entity.RotationSettingsEntity
import example.yf.fruit_hall.data.rotation.entity.RotationSlotEntity
import example.yf.fruit_hall.data.rotation.entity.ScheduleTemplateEntity
import example.yf.fruit_hall.data.rotation.entity.ShiftPresetEntity
import example.yf.fruit_hall.data.rotation.entity.ShiftRoleEntity

// 포지션 탭(position_database)과 별도인 신규 DB (§4 결정). 멤버 데이터는 memberId로만
// 참조하고 이름·색은 MemberDao Flow를 combine해서 읽는다 — Room은 DB 간 FK를 지원하지 않는다.
@Database(
    entities = [
        RotationPositionEntity::class,
        ShiftPresetEntity::class,
        ShiftRoleEntity::class,
        DayPlanEntity::class,
        DayRoleAssignmentEntity::class,
        BreakAssignmentEntity::class,
        RotationPlanEntity::class,
        RotationSlotEntity::class,
        RotationCellEntity::class,
        RotationSettingsEntity::class,
        ScheduleTemplateEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class RotationDatabase : RoomDatabase() {
    abstract fun rotationPositionDao(): RotationPositionDao
    abstract fun shiftPresetDao(): ShiftPresetDao
    abstract fun dayPlanDao(): DayPlanDao
    abstract fun rotationPlanDao(): RotationPlanDao
    abstract fun rotationSettingsDao(): RotationSettingsDao
    abstract fun scheduleTemplateDao(): ScheduleTemplateDao
}
