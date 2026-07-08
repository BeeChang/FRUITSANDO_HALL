package example.yf.fruit_hall.data.tray.db

import androidx.room.Database
import androidx.room.RoomDatabase
import example.yf.fruit_hall.data.tray.dao.AllocationHistoryDao
import example.yf.fruit_hall.data.tray.dao.AllocationSettingsDao
import example.yf.fruit_hall.data.tray.dao.ManualEditLogDao
import example.yf.fruit_hall.data.tray.dao.SnackTypeDao
import example.yf.fruit_hall.data.tray.dao.SpaceDao
import example.yf.fruit_hall.data.tray.dao.TrayDao
import example.yf.fruit_hall.data.tray.dao.TrayItemDao
import example.yf.fruit_hall.data.tray.entity.AllocationHistoryEntity
import example.yf.fruit_hall.data.tray.entity.AllocationSettingsEntity
import example.yf.fruit_hall.data.tray.entity.ManualEditLogEntity
import example.yf.fruit_hall.data.tray.entity.SnackTypeEntity
import example.yf.fruit_hall.data.tray.entity.SpaceEntity
import example.yf.fruit_hall.data.tray.entity.TrayEntity
import example.yf.fruit_hall.data.tray.entity.TrayItemEntity

@Database(
    entities = [
        SpaceEntity::class,
        SnackTypeEntity::class,
        TrayEntity::class,
        TrayItemEntity::class,
        AllocationSettingsEntity::class,
        AllocationHistoryEntity::class,
        ManualEditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TrayDatabase : RoomDatabase() {
    abstract fun spaceDao(): SpaceDao
    abstract fun snackTypeDao(): SnackTypeDao
    abstract fun trayDao(): TrayDao
    abstract fun trayItemDao(): TrayItemDao
    abstract fun allocationSettingsDao(): AllocationSettingsDao
    abstract fun allocationHistoryDao(): AllocationHistoryDao
    abstract fun manualEditLogDao(): ManualEditLogDao
}
