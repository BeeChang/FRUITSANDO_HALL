package example.yf.fruit_hall.data.tray.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tray_spaces")
data class SpaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val capacity: Int? = null // 이 공간에 실제로 놓을 수 있는 최대 판 수. null=제한 없음/미입력
)
