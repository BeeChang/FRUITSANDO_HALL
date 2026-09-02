package example.yf.fruit_hall.data.rotation.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 명세 §3-3. role 하나에 멤버 하나를 꽂고, 시각은 프리셋 값 복사 후 그날만 수정 가능(반차·지각).
// memberId는 position_database의 members를 가리키지만, 별도 DB라 FK를 걸지 않는다(§4).
@Entity(
    tableName = "day_role_assignments",
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
data class DayRoleAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayPlanId: Long,
    val roleLabel: String,
    val memberId: Long? = null, // null = 아직 인원배정 안 됨. 이 role은 오늘 그리드에서 제외된다
    val startMin: Int,
    val endMin: Int
)
