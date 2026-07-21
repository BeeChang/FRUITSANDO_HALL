package example.yf.fruit_hall.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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

// qtySensitivity 컬럼 제거(설정 화면에서 뺌). SQLite는 컬럼 DROP을 직접 지원하지 않아 테이블 재생성으로 처리.
private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE allocation_settings_new (" +
                "id INTEGER NOT NULL PRIMARY KEY, rounds INTEGER NOT NULL, capacityCsv TEXT NOT NULL, " +
                "flexDeviation INTEGER NOT NULL, allowedMissingTypes INTEGER NOT NULL, " +
                "primaryLocationSpaceId INTEGER, topN INTEGER NOT NULL, ilsIterations INTEGER NOT NULL, " +
                "spreadStrength TEXT NOT NULL, orderStrictness TEXT NOT NULL, moveAversion TEXT NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO allocation_settings_new (id, rounds, capacityCsv, flexDeviation, allowedMissingTypes, " +
                "primaryLocationSpaceId, topN, ilsIterations, spreadStrength, orderStrictness, moveAversion) " +
                "SELECT id, rounds, capacityCsv, flexDeviation, allowedMissingTypes, " +
                "primaryLocationSpaceId, topN, ilsIterations, spreadStrength, orderStrictness, moveAversion " +
                "FROM allocation_settings"
        )
        db.execSQL("DROP TABLE allocation_settings")
        db.execSQL("ALTER TABLE allocation_settings_new RENAME TO allocation_settings")
    }
}

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
        )
         // destructive fallback 없음: 스키마를 바꿀 땐 반드시 여기에 실제 Migration을 addMigrations로
         // 추가해야 한다. 빠뜨리면 마이그레이션 누락 예외로 즉시 드러나며, 조용히 데이터를 지우지 않는다.
         .addMigrations(MIGRATION_5_6)
         .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT INTO allocation_settings (id, rounds, capacityCsv, flexDeviation, " +
                        "allowedMissingTypes, primaryLocationSpaceId, topN, ilsIterations, " +
                        "spreadStrength, orderStrictness, moveAversion) " +
                        "VALUES (1, 3, 'X1.0,X1.0,X1.0', 2, 0, NULL, 5, 6, 'MID', 'MID', 'MID')"
                )
                // 위치(Space) 선택 UI가 항상 최소 1개는 고를 수 있도록 기본 공간을 만들어둔다
                db.execSQL("INSERT INTO tray_spaces (name, sortOrder) VALUES ('기본 진열대', 0)")
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
