package example.yf.fruit_hall.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import example.yf.fruit_hall.data.tray.dao.AllocationHistoryDao
import example.yf.fruit_hall.data.tray.dao.AllocationSettingsDao
import example.yf.fruit_hall.data.tray.dao.ManualEditLogDao
import example.yf.fruit_hall.data.tray.dao.SnackTypeDao
import example.yf.fruit_hall.data.tray.dao.SpaceDao
import example.yf.fruit_hall.data.tray.dao.TrayDao
import example.yf.fruit_hall.data.tray.dao.TrayItemDao
import example.yf.fruit_hall.data.tray.db.TrayDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrayDatabaseModule {

    @Provides
    @Singleton
    fun provideTrayDatabase(@ApplicationContext context: Context): TrayDatabase {
        return Room.databaseBuilder(
            context,
            TrayDatabase::class.java,
            "tray_database"
        ).fallbackToDestructiveMigration(true) // 개발 단계, 배포된 사용자 데이터 없음
         .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT INTO allocation_settings (id, rounds, capacityModeType, exactTraysPerRoundCsv, " +
                        "ratioPercentsCsv, maxDeviation, allowedMissingTypes, spreadStrength, orderStrictness, qtySensitivity) " +
                        "VALUES (1, 3, 'RATIO', '', '34,33,33', 2, 0, 'MID', 'MID', 'MID')"
                )
            }
        }).build()
    }

    @Provides
    @Singleton
    fun provideSpaceDao(db: TrayDatabase): SpaceDao = db.spaceDao()

    @Provides
    @Singleton
    fun provideSnackTypeDao(db: TrayDatabase): SnackTypeDao = db.snackTypeDao()

    @Provides
    @Singleton
    fun provideTrayDao(db: TrayDatabase): TrayDao = db.trayDao()

    @Provides
    @Singleton
    fun provideTrayItemDao(db: TrayDatabase): TrayItemDao = db.trayItemDao()

    @Provides
    @Singleton
    fun provideAllocationSettingsDao(db: TrayDatabase): AllocationSettingsDao = db.allocationSettingsDao()

    @Provides
    @Singleton
    fun provideAllocationHistoryDao(db: TrayDatabase): AllocationHistoryDao = db.allocationHistoryDao()

    @Provides
    @Singleton
    fun provideManualEditLogDao(db: TrayDatabase): ManualEditLogDao = db.manualEditLogDao()
}
