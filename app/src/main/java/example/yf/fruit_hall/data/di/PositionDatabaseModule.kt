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

    @Provides
    @Singleton
    fun providePositionDatabase(@ApplicationContext context: Context): PositionDatabase {
        return Room.databaseBuilder(
            context,
            PositionDatabase::class.java,
            "position_database"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    """INSERT INTO positions (name, isMultiPerson, hasWeight, weightStrength, weightDecayMode, sortOrder)
                       VALUES ('캐셔', 0, 1, 0.85, 'STAY_LOW', 0)"""
                )
                db.execSQL(
                    """INSERT INTO positions (name, isMultiPerson, hasWeight, weightStrength, weightDecayMode, sortOrder)
                       VALUES ('문지기', 0, 1, 0.7, 'STAY_LOW', 1)"""
                )
                db.execSQL(
                    """INSERT INTO positions (name, isMultiPerson, hasWeight, weightStrength, weightDecayMode, sortOrder)
                       VALUES ('캐셔서포트', 1, 0, 0.8, 'STAY_LOW', 2)"""
                )
                db.execSQL(
                    """INSERT INTO positions (name, isMultiPerson, hasWeight, weightStrength, weightDecayMode, sortOrder)
                       VALUES ('음료제작', 1, 0, 0.8, 'STAY_LOW', 3)"""
                )
                db.execSQL(
                    """INSERT INTO slot_settings (id, totalSlots) VALUES (1, 3)"""
                )
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
