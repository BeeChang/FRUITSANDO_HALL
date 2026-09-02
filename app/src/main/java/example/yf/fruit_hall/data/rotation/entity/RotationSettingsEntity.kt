package example.yf.fruit_hall.data.rotation.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// 단일 행(id=1) 설정 테이블. onCreate 시드는 이 행 하나뿐 — 포지션·프리셋은 절대 심지 않는다 (§14-8).
// tierOrderCsv 예: "CUMULATIVE_HIGH,CONSTRAINT,HIGH_VARIETY,LOW_FAIRNESS,TIEBREAK"
@Entity(tableName = "rotation_settings")
data class RotationSettingsEntity(
    @PrimaryKey val id: Int = 1,

    // 시간 (§10-7 설정화면 3)
    val windowStart: Int = 11 * 60,
    val windowEnd: Int = 19 * 60,
    val preBreakDesiredMinutes: Int = 50,
    val inBreakDesiredMinutes: Int? = null,
    val postBreakDesiredMinutes: Int = 60,
    val minSlotMinutes: Int = 40,
    val defaultBreakStartMin: Int = 13 * 60 + 30, // §3-4 기본 브레이크 생성 시작 시각
    // MIGRATION_2_3이 raw SQL로 DEFAULT 540을 붙였으므로 Room이 기대하는 스키마도 맞춰준다 (안 맞으면 마이그레이션 후 검증 크래시)
    @ColumnInfo(defaultValue = "540")
    val defaultShiftDurationMinutes: Int = 9 * 60, // 근무 스케줄 출근 입력 시 퇴근 자동 채움 기본 길이
    val remainderPolicy: String = "REDISTRIBUTE",
    val handoverMode: String = "DISPLAY_ONLY",
    val handoverMinutes: Int = 5,
    val handoverMinSlotMinutes: Int = 20,

    // 공평성 (설정화면 4)
    val alpha: Double = 1.0,
    val targetBasis: String = "TOTAL_MINUTES",
    val fairnessPriority: String = "HIGH",
    val midBandMinutes: Int = 15,

    // 제약 (설정화면 5)
    val samePositionMaxRun: Int = 1,
    val breakInterruptsRun: Boolean = true,
    val allowHighChain: Boolean = true,
    val highMaxRun: Int = 2,
    val highCooldownSlots: Int = 0,
    val relaxPreBreak: Boolean = true,

    // 계층 우선순위 (설정화면 6)
    val tierOrderCsv: String = "CUMULATIVE_HIGH,CONSTRAINT,HIGH_VARIETY,LOW_FAIRNESS,TIEBREAK",

    // 탐색 (설정화면 7)
    val ilsIterations: Int = 6,

    // §9 freezeCursor 기본값: 진행 중 슬롯을 확정으로 볼지, 재생성 대상으로 볼지
    val freezeCurrentSlot: Boolean = true
)
