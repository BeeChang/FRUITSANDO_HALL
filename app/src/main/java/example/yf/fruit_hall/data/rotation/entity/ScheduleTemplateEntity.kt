package example.yf.fruit_hall.data.rotation.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// 프리셋에 종속되지 않는 재사용 가능한 근무 스케줄 모양(출근·퇴근·브레이크). 여러 프리셋에서 끌어다 쓴다.
@Entity(tableName = "schedule_templates")
data class ScheduleTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val startMin: Int,
    val endMin: Int,
    // 순번이 아니라 실제 시작 시각(분) — 물리 컬럼명은 과거 breakOrder 그대로 두고 의미만 바꿔 마이그레이션을 피했다.
    @ColumnInfo(name = "breakOrder")
    val breakStartMin: Int? = null,
    val breakMinutes: Int = 60,
    val sortOrder: Int = 0
)
