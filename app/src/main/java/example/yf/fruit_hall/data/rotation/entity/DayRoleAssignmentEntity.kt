package example.yf.fruit_hall.data.rotation.entity

import androidx.room.ColumnInfo
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
    val endMin: Int,
    // 오늘 이 줄에 적용한 근무 스케줄 템플릿 이름(오픈·미들·마감 등). 표의 이름 칸에 그대로 보여준다.
    // 시각을 역으로 매칭해 알아내지 않는다 — 시각이 같은 템플릿이 여럿이거나 손으로 고치면 틀린다.
    val scheduleLabel: String? = null,
    // 오늘 이 사람은 표에는 보이되 포지션 배정에서 뺀다(교육·행사 지원 등). 근무 자체를 지우는 게
    // 아니라서 근무시간·휴게는 그대로 두고, 알고리즘 입력(workers)에서만 제외한다.
    @ColumnInfo(defaultValue = "0")
    val excludedFromAssign: Boolean = false
)
