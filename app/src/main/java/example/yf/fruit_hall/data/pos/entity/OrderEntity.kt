package example.yf.fruit_hall.data.pos.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: Long,
    val date: String,
    val itemsText: String,
    val totalAmount: Int,
    val receiptPhone: String = "-",
    val pointPhone: String = "-",
    val status: String = "pending",
)
