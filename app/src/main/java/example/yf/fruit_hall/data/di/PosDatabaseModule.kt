package example.yf.fruit_hall.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import example.yf.fruit_hall.data.pos.dao.OrderDao
import example.yf.fruit_hall.data.pos.dao.ProductDao
import example.yf.fruit_hall.data.pos.db.PosDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PosDatabaseModule {

    @Provides
    @Singleton
    fun providePosDatabase(@ApplicationContext context: Context): PosDatabase =
        Room.databaseBuilder(context, PosDatabase::class.java, "pos_database").build()

    @Provides
    @Singleton
    fun provideProductDao(db: PosDatabase): ProductDao = db.productDao()

    @Provides
    @Singleton
    fun provideOrderDao(db: PosDatabase): OrderDao = db.orderDao()
}
