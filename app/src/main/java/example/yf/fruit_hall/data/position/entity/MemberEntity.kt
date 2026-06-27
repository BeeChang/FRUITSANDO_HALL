package example.yf.fruit_hall.data.position.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isWorking: Boolean = false,
    val sortOrder: Int = 0,
    val colorHex: String = "#4D96FF"
)