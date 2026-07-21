package example.yf.fruit_hall.data.tray.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 단일 행(id=1) 설정 테이블.
 * capacityCsv: 차수별 캐패시티, 콤마 구분. 토큰 형식 "F<판수>"(Fixed) 또는 "X<weight>"(Flexible).
 * 예) "F5,X1.0,X1.0" — 1차는 5판 고정, 2·3차는 잔여를 1:1 비율로 자동 배분.
 */
@Entity(tableName = "allocation_settings")
data class AllocationSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val rounds: Int = 3,
    val capacityCsv: String = "X1.0,X1.0,X1.0",
    val flexDeviation: Int = 2,
    val allowedMissingTypes: Int = 0,
    val primaryLocationSpaceId: Long? = null,
    val topN: Int = 5,
    val ilsIterations: Int = 6,
    val spreadStrength: String = "MID",
    val orderStrictness: String = "MID",
    val moveAversion: String = "MID"
)
