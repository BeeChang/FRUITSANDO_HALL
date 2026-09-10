package example.yf.fruit_hall.data.rotation.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 명세 §3-4. "순번"은 저장하지 않는다 — 순번은 기본값 생성 규칙일 뿐, 저장되는 건 실제 시각이다.
@Entity(
    tableName = "break_assignments",
    foreignKeys = [
        ForeignKey(
            entity = DayPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayPlanId")]
)
data class BreakAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayPlanId: Long,
    val memberId: Long,
    val startMin: Int,
    val endMin: Int,
    val isManual: Boolean = false
)
