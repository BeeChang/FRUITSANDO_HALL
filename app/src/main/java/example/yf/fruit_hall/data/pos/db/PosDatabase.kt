package example.yf.fruit_hall.data.pos.db

import androidx.room.Database
import androidx.room.RoomDatabase
import example.yf.fruit_hall.data.pos.dao.OrderDao
import example.yf.fruit_hall.data.pos.dao.ProductDao
import example.yf.fruit_hall.data.pos.entity.OrderEntity
import example.yf.fruit_hall.data.pos.entity.ProductEntity

@Database(
    entities = [ProductEntity::class, OrderEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
}