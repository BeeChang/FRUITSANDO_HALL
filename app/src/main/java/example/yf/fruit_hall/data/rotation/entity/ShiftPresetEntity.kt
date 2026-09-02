package example.yf.fruit_hall.data.rotation.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 명세 §3-2. 편성을 통째로 저장해두고 아침에 하나 골라 사람만 꽂는다.
@Entity(tableName = "shift_presets")
data class ShiftPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    val alphaOverride: Double? = null // null = 전역 설정 사용. 4인 편성처럼 오전 부담이 다른 프리셋용
)
