package example.yf.fruit_hall.data.tray.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 유저 수동 차수 수정 전/후 기록. 추후 가중치 튜닝 데이터원 */
@Entity(tableName = "manual_edit_log")
data class ManualEditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trayId: Long,
    val fromRound: Int,
    val toRound: Int,
    val timestamp: Long = System.currentTimeMillis()
)
