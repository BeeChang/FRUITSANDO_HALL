package example.yf.fruit_hall.data.rotation.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 명세 §3-5. 시드는 반드시 저장한다 — 랜덤 모드일수록 재현이 더 중요하다 (§7-5).
@Entity(
    tableName = "rotation_plans",
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
data class RotationPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayPlanId: Long,
    val seed: Long,
    val score: Long,
    val freezeCursorMin: Int
)
