package example.yf.fruit_hall.data.rotation.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 프리셋에 종속되지 않는 재사용 가능한 근무 스케줄 모양(출근·퇴근·브레이크). 여러 프리셋에서 끌어다 쓴다.
@Entity(tableName = "schedule_templates")
data class ScheduleTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val startMin: Int,
    val endMin: Int,
    val breakOrder: Int? = null,
    val breakMinutes: Int = 60,
    val sortOrder: Int = 0
)
