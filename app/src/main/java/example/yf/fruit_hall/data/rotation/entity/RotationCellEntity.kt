package example.yf.fruit_hall.data.rotation.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 명세 §3-5. 사람 × 슬롯. 셀 단위 저장이라 드래그 수정·핀·필터 뷰가 전부 이 테이블 위에서 돌아간다.
// positionId는 rotation_positions를 가리키지만, 삭제된 포지션의 과거 기록 보존을 위해 FK를 걸지 않는다
// (프로젝트 관례: PositionDatabase의 소프트 삭제와 동일한 이유, §4의 memberId 처리와 일관).
@Entity(
    tableName = "rotation_cells",
    foreignKeys = [
        ForeignKey(
            entity = RotationPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId"), Index(value = ["planId", "slotIndex"])]
)
data class RotationCellEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val slotIndex: Int,
    val memberId: Long,
    val positionId: Long? = null,
    val cellState: String,       // CellState.name
    val isPinned: Boolean = false,
    val isManuallyEdited: Boolean = false
)
