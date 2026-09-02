package example.yf.fruit_hall.data.rotation.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 명세 §3-1. 기본 시드 없음 — 사용자가 직접 전부 생성한다.
@Entity(tableName = "rotation_positions")
data class RotationPositionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val intensity: String,          // Intensity.name: "HIGH"/"LOW"
    val minCount: Int,
    val maxCount: Int? = null,      // null = 무제한
    val openPriority: Int,
    val overflowPriority: Int? = null, // null = 잉여 안 받음
    val isActive: Boolean = true,
    val colorHex: String = "#7FD1D1",
    val sortOrder: Int = 0
)
