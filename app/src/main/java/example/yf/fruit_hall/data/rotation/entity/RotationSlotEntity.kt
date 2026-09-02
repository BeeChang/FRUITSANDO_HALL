package example.yf.fruit_hall.data.rotation.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rotation_slots",
    foreignKeys = [
        ForeignKey(
            entity = RotationPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class RotationSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val slotIndex: Int,
    val startMin: Int,
    val endMin: Int,
    val segmentType: String,     // SegmentType.name
    val isFrozen: Boolean = false,
    val remainderNote: String? = null
)
