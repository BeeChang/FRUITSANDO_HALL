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
import example.yf.fruit_hall.data.position.dao.AssignmentDao
import example.yf.fruit_hall.data.position.dao.MemberDao
import example.yf.fruit_hall.data.position.dao.PositionDao
import example.yf.fruit_hall.data.position.dao.SlotSettingsDao
import example.yf.fruit_hall.data.position.dao.WorkDayDao
import example.yf.fruit_hall.data.position.db.PositionDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PositionDatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE members ADD COLUMN colorHex TEXT NOT NULL DEFAULT '#4D96FF'")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 기존 진한 색상을 파스텔로 교체 (새 팔레트 색상은 유지)
            db.execSQL("""
                UPDATE members
                SET colorHex = CASE (id % 8)
                    WHEN 0 THEN '#FFB3C6'
                    WHEN 1 THEN '#B3D9FF'
                    WHEN 2 THEN '#A8E6CF'
                    WHEN 3 THEN '#D4B8F5'
                    WHEN 4 THEN '#FFD6A5'
                    WHEN 5 THEN '#FFAFD0'
                    WHEN 6 THEN '#C9B2F0'
                    ELSE '#B5EAD7'
                END
                WHERE colorHex NOT IN (
                    '#FFB3C6','#FF8FAB','#FFAFD0','#FFD6E5',
                    '#F9C0D0','#FFA8C0','#F0C0D8','#F5C6EC',
                    '#D4B8F5','#C9B2F0','#E2CCFF','#D0B4FF',
                    '#B8AFEF','#DEBEFF','#ECD5E3','#F0D0FF',
                    '#B3D9FF','#A8D0F8','#BDD7FF','#C8DCFF',
                    '#A8E6CF','#B5EAD7','#C7F2D4','#9FD8D8',
                    '#B0E0E6','#C8ECC8','#C5E8C5','#D4F0C8',
                    '#FFD6A5','#FFE5B4','#FFEAA7','#FFD0A8'
                )
            """.trimIndent())
        }
    }

    @Provides
    @Singleton
    fun providePositionDatabase(@ApplicationContext context: Context): PositionDatabase {
        return Room.databaseBuilder(
            context,
            PositionDatabase::class.java,
            "position_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
         .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("INSERT INTO slot_settings (id, totalSlots) VALUES (1, 3)")
            }
        }).build()
    }

    @Provides
    @Singleton
    fun provideMemberDao(db: PositionDatabase): MemberDao = db.memberDao()

    @Provides
    @Singleton
    fun providePositionDao(db: PositionDatabase): PositionDao = db.positionDao()

    @Provides
    @Singleton
    fun provideWorkDayDao(db: PositionDatabase): WorkDayDao = db.workDayDao()

    @Provides
    @Singleton
    fun provideSlotSettingsDao(db: PositionDatabase): SlotSettingsDao = db.slotSettingsDao()

    @Provides
    @Singleton
    fun provideAssignmentDao(db: PositionDatabase): AssignmentDao = db.assignmentDao()
}
