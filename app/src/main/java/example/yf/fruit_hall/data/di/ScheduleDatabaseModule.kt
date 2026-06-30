package example.yf.fruit_hall.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import example.yf.fruit_hall.data.schedule.dao.ScheduleDao
import example.yf.fruit_hall.data.schedule.db.ScheduleDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScheduleDatabaseModule {

    @Provides
    @Singleton
    fun provideScheduleDatabase(@ApplicationContext context: Context): ScheduleDatabase =
        Room.databaseBuilder(context, ScheduleDatabase::class.java, "schedule_database").build()

    @Provides
    @Singleton
    fun provideScheduleDao(db: ScheduleDatabase): ScheduleDao = db.scheduleDao()
}
