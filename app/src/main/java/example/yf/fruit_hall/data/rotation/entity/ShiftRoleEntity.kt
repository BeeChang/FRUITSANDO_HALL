package example.yf.fruit_hall.data.rotation.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 명세 §3-2. 프리셋에 N개, 개수 제한 없음. 출근·퇴근은 분 단위 임의 값(10:30, 12:15 전부 가능).
@Entity(
    tableName = "shift_roles",
    foreignKeys = [
        ForeignKey(
            entity = ShiftPresetEntity::class,
            parentColumns = ["id"],
            childColumns = ["presetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("presetId")]
)
data class ShiftRoleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetId: Long,
    val label: String,
    val startMin: Int,
    val endMin: Int,
    val breakOrder: Int? = null,   // null = 브레이크 없음 (반차 등)
    val breakMinutes: Int = 60,
    val sortOrder: Int = 0,
    // MIGRATION_1_2가 raw SQL로 DEFAULT 1을 붙였으므로, Room이 기대하는 스키마도 반드시 맞춰준다
    // (안 맞으면 마이그레이션 뒤 스키마 검증에서 크래시 — §Room 마이그레이션 필수 규칙과 별개의 함정).
    @ColumnInfo(defaultValue = "1")
    val isActive: Boolean = true   // 삭제 대신 껐다 켰다 — 꺼진 role은 오늘 인원배정·재생성에서 제외
)
