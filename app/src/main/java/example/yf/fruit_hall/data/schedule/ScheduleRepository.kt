package example.yf.fruit_hall.data.schedule

import example.yf.fruit_hall.data.RemoteConfigRepository
import example.yf.fruit_hall.data.schedule.dao.ScheduleDao
import example.yf.fruit_hall.data.schedule.entity.ScheduleEntryEntity
import example.yf.fruit_hall.data.schedule.entity.ScheduleMetadataEntity
import example.yf.fruit_hall.data.schedule.model.ScheduleResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val scheduleDao: ScheduleDao
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val syncIntervalMs = 24 * 60 * 60 * 1000L // 하루

    suspend fun syncAll(monthKeys: List<String>, force: Boolean = false) {
        val staleKeys = if (force) monthKeys else monthKeys.filter { key ->
            val meta = scheduleDao.getMetadata(key)
            meta == null || System.currentTimeMillis() - meta.syncedAt >= syncIntervalMs
        }
        if (staleKeys.isEmpty()) {
            Timber.d("[Schedule] 모든 월 캐시 유효, skip")
            return
        }

        remoteConfigRepository.fetchAndActivate(force = force)

        staleKeys.forEach { monthKey ->
            runCatching {
                val jsonStr = remoteConfigRepository.getScheduleJson(monthKey) ?: run {
                    Timber.d("[Schedule] $monthKey → Remote Config에 데이터 없음, skip")
                    return@forEach
                }
                val response = json.decodeFromString<ScheduleResponse>(jsonStr)
                val existing = scheduleDao.getMetadata(monthKey)
                if (existing?.updated == response.updated) {
                    scheduleDao.upsertMetadata(existing.copy(syncedAt = System.currentTimeMillis()))
                    Timber.d("[Schedule] $monthKey → updated 동일, syncedAt만 갱신")
                    return@forEach
                }
                val entries = response.schedules.flatMap { day ->
                    day.shifts.map { (person, shift) ->
                        ScheduleEntryEntity(monthKey = monthKey, date = day.date, personName = person, shift = shift)
                    }
                }
                scheduleDao.replaceMonthData(
                    ScheduleMetadataEntity(monthKey, response.updated, System.currentTimeMillis()),
                    entries
                )
                Timber.d("[Schedule] $monthKey → DB 갱신 완료 (${entries.size}건)")
            }.onFailure { Timber.e(it, "[Schedule] $monthKey sync 실패") }
        }
    }

    fun observeEntriesForMonth(monthKey: String): Flow<List<ScheduleEntryEntity>> =
        scheduleDao.observeEntriesForMonth(monthKey)

    suspend fun hasDataForMonth(monthKey: String): Boolean =
        scheduleDao.countEntries(monthKey) > 0
}
