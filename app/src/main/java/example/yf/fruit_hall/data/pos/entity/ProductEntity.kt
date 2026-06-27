package example.yf.fruit_hall.data.pos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Int,
    val category: String,
    val bgColor: String = "#ffffff",
    val sortOrder: Int = 0,
)
