package example.yf.fruit_hall.data.tray.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 단일 행(id=1) 설정 테이블. capacityModeType: "EXACT" 또는 "RATIO" */
@Entity(tableName = "allocation_settings")
data class AllocationSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val rounds: Int = 3,
    val capacityModeType: String = "RATIO",
    // Exact 모드: 콤마 구분, 잔여(remainder) 차수는 빈 토큰. 예) "5,5," (3차가 잔여)
    val exactTraysPerRoundCsv: String = "",
    // Ratio 모드: 콤마 구분 퍼센트. 예) "34,33,33"
    val ratioPercentsCsv: String = "34,33,33",
    val maxDeviation: Int = 2,
    val allowedMissingTypes: Int = 0,
    val spreadStrength: String = "MID",
    val orderStrictness: String = "MID",
    val qtySensitivity: String = "MID"
)
