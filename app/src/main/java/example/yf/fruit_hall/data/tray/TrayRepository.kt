package example.yf.fruit_hall.data.tray

import example.yf.fruit_hall.core.AllocationConfig
import example.yf.fruit_hall.core.CapacityMode
import example.yf.fruit_hall.core.Level
import example.yf.fruit_hall.core.RoughSize
import example.yf.fruit_hall.core.Tray
import example.yf.fruit_hall.core.TrayItem
import example.yf.fruit_hall.core.TuningPresets
import example.yf.fruit_hall.data.tray.dao.AllocationHistoryDao
import example.yf.fruit_hall.data.tray.dao.AllocationSettingsDao
import example.yf.fruit_hall.data.tray.dao.ManualEditLogDao
import example.yf.fruit_hall.data.tray.dao.SnackTypeDao
import example.yf.fruit_hall.data.tray.dao.SpaceDao
import example.yf.fruit_hall.data.tray.dao.TrayDao
import example.yf.fruit_hall.data.tray.dao.TrayItemDao
import example.yf.fruit_hall.data.tray.entity.AllocationHistoryEntity
import example.yf.fruit_hall.data.tray.entity.AllocationSettingsEntity
import example.yf.fruit_hall.data.tray.entity.ManualEditLogEntity
import example.yf.fruit_hall.data.tray.entity.SnackTypeEntity
import example.yf.fruit_hall.data.tray.entity.SpaceEntity
import example.yf.fruit_hall.data.tray.entity.TrayEntity
import example.yf.fruit_hall.data.tray.entity.TrayItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrayRepository @Inject constructor(
    private val spaceDao: SpaceDao,
    private val snackTypeDao: SnackTypeDao,
    private val trayDao: TrayDao,
    private val trayItemDao: TrayItemDao,
    private val settingsDao: AllocationSettingsDao,
    private val historyDao: AllocationHistoryDao,
    private val editLogDao: ManualEditLogDao,
) {
    fun observeSpaces(): Flow<List<SpaceEntity>> = spaceDao.observeAll()
    fun observeSnackTypes(): Flow<List<SnackTypeEntity>> = snackTypeDao.observeAll()
    fun observeTrays(): Flow<List<TrayEntity>> = trayDao.observeAll()
    fun observeTrayItems(): Flow<List<TrayItemEntity>> = trayItemDao.observeAll()
    fun observeSettings(): Flow<AllocationSettingsEntity> =
        settingsDao.observe().map { it ?: AllocationSettingsEntity() }
    fun observeHistory(): Flow<List<AllocationHistoryEntity>> = historyDao.observeRecent()

    suspend fun addSpace(name: String, sortOrder: Int) =
        spaceDao.insert(SpaceEntity(name = name, sortOrder = sortOrder))

    suspend fun deleteSpace(space: SpaceEntity) = spaceDao.delete(space)

    suspend fun addSnackType(name: String, colorHex: String, secondaryColorHex: String?, sortOrder: Int) =
        snackTypeDao.insert(
            SnackTypeEntity(name = name, colorHex = colorHex, secondaryColorHex = secondaryColorHex, sortOrder = sortOrder)
        )

    suspend fun deleteSnackType(snackType: SnackTypeEntity) = snackTypeDao.delete(snackType)

    suspend fun reorderSnackTypes(orderedIds: List<Long>) = snackTypeDao.updateSortOrders(orderedIds)

    suspend fun addTray(spaceId: Long, sortOrder: Int): Long =
        trayDao.insert(TrayEntity(spaceId = spaceId, sortOrder = sortOrder))

    suspend fun deleteTray(tray: TrayEntity) = trayDao.delete(tray)

    suspend fun addItemToTray(trayId: Long, snackTypeId: Long, roughSize: RoughSize?, exactQty: Int? = null) =
        trayItemDao.insert(
            TrayItemEntity(trayId = trayId, snackTypeId = snackTypeId, exactQty = exactQty, roughSize = roughSize?.name)
        )

    suspend fun updateItemRoughSize(trayId: Long, snackTypeId: Long, roughSize: RoughSize) =
        trayItemDao.updateRoughSize(trayId, snackTypeId, roughSize.name)

    suspend fun removeItemFromTray(trayId: Long, snackTypeId: Long) =
        trayItemDao.deleteByTrayAndType(trayId, snackTypeId)

    suspend fun setPinnedRound(trayId: Long, round: Int?) = trayDao.setPinnedRound(trayId, round)

    suspend fun resetDay() = trayDao.deleteAll() // tray_items는 FK CASCADE로 함께 삭제

    suspend fun updateSettings(settings: AllocationSettingsEntity) = settingsDao.upsert(settings)

    suspend fun confirmAllocation(date: String, assignment: Map<Long, Int>, roundSizes: List<Int>, score: Int) {
        historyDao.insert(
            AllocationHistoryEntity(
                date = date,
                assignmentCsv = assignment.entries.joinToString(",") { "${it.key}:${it.value}" },
                roundSizesCsv = roundSizes.joinToString(","),
                score = score
            )
        )
    }

    suspend fun logManualEdit(trayId: Long, fromRound: Int, toRound: Int) =
        editLogDao.insert(ManualEditLogEntity(trayId = trayId, fromRound = fromRound, toRound = toRound))

    /** 데이터 계층 join 책임: Room 엔티티 → 엔진 순수 도메인 모델 */
    fun buildEngineTrays(trays: List<TrayEntity>, items: List<TrayItemEntity>): List<Tray> {
        val itemsByTray = items.groupBy { it.trayId }
        return trays.map { tray ->
            val trayItems = itemsByTray[tray.id].orEmpty().map { item ->
                TrayItem(
                    typeId = item.snackTypeId.toString(),
                    exactQty = item.exactQty,
                    roughSize = item.roughSize?.let { RoughSize.valueOf(it) }
                )
            }
            Tray(id = tray.id.toString(), items = trayItems, pinnedRound = tray.pinnedRound)
        }
    }

    fun buildAllocationConfig(settings: AllocationSettingsEntity): AllocationConfig {
        val capacity = if (settings.capacityModeType == "EXACT") {
            CapacityMode.Exact(parseExactCsv(settings.exactTraysPerRoundCsv, settings.rounds))
        } else {
            CapacityMode.Ratio(parseRatioCsv(settings.ratioPercentsCsv, settings.rounds), settings.maxDeviation)
        }
        return AllocationConfig(
            rounds = settings.rounds,
            capacity = capacity,
            allowedMissingTypes = settings.allowedMissingTypes,
            presets = TuningPresets(
                spreadStrength = Level.valueOf(settings.spreadStrength),
                orderStrictness = Level.valueOf(settings.orderStrictness),
                qtySensitivity = Level.valueOf(settings.qtySensitivity)
            )
        )
    }

    private fun parseExactCsv(csv: String, rounds: Int): List<Int?> {
        val tokens = csv.split(",")
        return List(rounds) { i -> tokens.getOrNull(i)?.trim()?.toIntOrNull() }
    }

    private fun parseRatioCsv(csv: String, rounds: Int): List<Double> {
        val tokens = csv.split(",").mapNotNull { it.trim().toDoubleOrNull() }
        return if (tokens.size == rounds) tokens else List(rounds) { 100.0 / rounds }
    }

    companion object {
        fun exactCsv(traysPerRound: List<Int?>): String =
            traysPerRound.joinToString(",") { it?.toString() ?: "" }

        fun ratioCsv(percents: List<Double>): String =
            percents.joinToString(",") { it.toString() }
    }
}
