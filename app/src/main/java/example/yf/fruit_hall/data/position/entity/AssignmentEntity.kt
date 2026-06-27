package example.yf.fruit_hall.data.position.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PositionEntity::class,
            parentColumns = ["id"],
            childColumns = ["positionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId"), Index("positionId")]
)
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val slotNumber: Int,
    val memberId: Long,
    val positionId: Long,
    val confirmedAt: Long = System.currentTimeMillis()
)