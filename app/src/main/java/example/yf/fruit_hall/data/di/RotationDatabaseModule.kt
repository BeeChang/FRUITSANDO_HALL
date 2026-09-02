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
import example.yf.fruit_hall.data.rotation.dao.DayPlanDao
import example.yf.fruit_hall.data.rotation.dao.RotationPlanDao
import example.yf.fruit_hall.data.rotation.dao.RotationPositionDao
import example.yf.fruit_hall.data.rotation.dao.RotationSettingsDao
import example.yf.fruit_hall.data.rotation.dao.ScheduleTemplateDao
import example.yf.fruit_hall.data.rotation.dao.ShiftPresetDao
import example.yf.fruit_hall.data.rotation.db.RotationDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RotationDatabaseModule {

    // 역할을 삭제 대신 껐다 켤 수 있도록 isActive 컬럼 추가
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE shift_roles ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
        }
    }

    // 출근 입력 시 퇴근을 자동으로 채워줄 기본 근무시간(분) 설정 컬럼 추가
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE rotation_settings ADD COLUMN defaultShiftDurationMinutes INTEGER NOT NULL DEFAULT 540")
        }
    }

    // 프리셋에 종속되지 않는 재사용 가능한 근무 스케줄 템플릿 테이블 추가
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS schedule_templates (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "label TEXT NOT NULL, " +
                    "startMin INTEGER NOT NULL, " +
                    "endMin INTEGER NOT NULL, " +
                    "breakOrder INTEGER, " +
                    "breakMinutes INTEGER NOT NULL, " +
                    "sortOrder INTEGER NOT NULL)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideRotationDatabase(@ApplicationContext context: Context): RotationDatabase {
        return Room.databaseBuilder(
            context,
            RotationDatabase::class.java,
            "rotation_database"
        )
            // destructive fallback 없음: 스키마를 바꿀 땐 반드시 여기에 실제 Migration을 추가해야 한다.
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // rotation_settings 기본 행 1개만 시드한다. 포지션·프리셋은 절대 심지 않는다 (§14-8).
                    // raw SQL은 Kotlin 기본값을 모르므로 모든 NOT NULL 컬럼에 명시적으로 값을 채운다.
                    db.execSQL(
                        "INSERT INTO rotation_settings (" +
                            "id, windowStart, windowEnd, preBreakDesiredMinutes, inBreakDesiredMinutes, " +
                            "postBreakDesiredMinutes, minSlotMinutes, defaultBreakStartMin, remainderPolicy, " +
                            "handoverMode, handoverMinutes, handoverMinSlotMinutes, alpha, targetBasis, " +
                            "fairnessPriority, midBandMinutes, samePositionMaxRun, breakInterruptsRun, " +
                            "allowHighChain, highMaxRun, highCooldownSlots, relaxPreBreak, tierOrderCsv, " +
                            "ilsIterations, freezeCurrentSlot, defaultShiftDurationMinutes" +
                            ") VALUES (" +
                            "1, 660, 1140, 50, NULL, " +
                            "60, 40, 810, 'REDISTRIBUTE', " +
                            "'DISPLAY_ONLY', 5, 20, 1.0, 'TOTAL_MINUTES', " +
                            "'HIGH', 15, 1, 1, " +
                            "1, 2, 0, 1, 'CUMULATIVE_HIGH,CONSTRAINT,HIGH_VARIETY,LOW_FAIRNESS,TIEBREAK', " +
                            "6, 1, 540)"
                    )
                }
            }).build()
    }

    @Provides
    @Singleton
    fun provideRotationPositionDao(db: RotationDatabase): RotationPositionDao = db.rotationPositionDao()

    @Provides
    @Singleton
    fun provideShiftPresetDao(db: RotationDatabase): ShiftPresetDao = db.shiftPresetDao()

    @Provides
    @Singleton
    fun provideDayPlanDao(db: RotationDatabase): DayPlanDao = db.dayPlanDao()

    @Provides
    @Singleton
    fun provideRotationPlanDao(db: RotationDatabase): RotationPlanDao = db.rotationPlanDao()

    @Provides
    @Singleton
    fun provideRotationSettingsDao(db: RotationDatabase): RotationSettingsDao = db.rotationSettingsDao()

    @Provides
    @Singleton
    fun provideScheduleTemplateDao(db: RotationDatabase): ScheduleTemplateDao = db.scheduleTemplateDao()
}
