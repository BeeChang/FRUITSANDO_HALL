package example.yf.fruit_hall.data.tray.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** '확정' 시 저장되는 스냅샷. assignmentCsv: "trayId:round,trayId:round,..." */
@Entity(tableName = "allocation_history")
data class AllocationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val assignmentCsv: String,
    val roundSizesCsv: String,
    val score: Int,
    val confirmedAt: Long = System.currentTimeMillis()
)
