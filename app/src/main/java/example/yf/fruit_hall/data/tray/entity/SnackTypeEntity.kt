package example.yf.fruit_hall.data.tray.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snack_types")
data class SnackTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#FFB3C6",
    val secondaryColorHex: String? = null, // 반반 혼합 색. null이면 단색
    val sortOrder: Int = 0,
    val isActive: Boolean = true // false=오늘 재고 이슈 등으로 미사용. 팔레트/판추가에서 제외, 결측 판정 대상에서도 제외
)
