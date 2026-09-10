package example.yf.fruit_hall.data.rotation.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 명세 §3-3. 프리셋을 복사해 그날 값으로 만든다 — 원본 프리셋은 절대 수정하지 않는다.
@Entity(tableName = "day_plans")
data class DayPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,           // "yyyy-MM-dd"
    val presetId: Long,
    val seed: Long,
    val settingsSnapshot: String // 그날 사용한 설정 전체 (JSON)
)
