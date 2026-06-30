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

    suspend fun syncIfNeeded(monthKey: String) {
        val jsonStr = remoteConfigRepository.fetchScheduleJson(monthKey) ?: run {
            Timber.d("[Schedule] $monthKey → Remote Config에 데이터 없음, skip")
            return
        }
        val response = json.decodeFromString<ScheduleResponse>(jsonStr)

        val existing = scheduleDao.getMetadata(monthKey)
        if (existing?.updated == response.updated) {
            Timber.d("[Schedule] $monthKey → updated(${response.updated}) 동일, skip")
            return
        }

        val entries = response.schedules.flatMap { day ->
            day.shifts.map { (person, shift) ->
                ScheduleEntryEntity(
                    monthKey = monthKey,
                    date = day.date,
                    personName = person,
                    shift = shift
                )
            }
        }
        val metadata = ScheduleMetadataEntity(
            monthKey = monthKey,
            updated = response.updated,
            syncedAt = System.currentTimeMillis()
        )
        scheduleDao.replaceMonthData(metadata, entries)
        Timber.d("[Schedule] $monthKey → DB 갱신 완료 (${entries.size}건, updated=${response.updated})")
    }

    fun observeEntriesForMonth(monthKey: String): Flow<List<ScheduleEntryEntity>> =
        scheduleDao.observeEntriesForMonth(monthKey)

    suspend fun hasDataForMonth(monthKey: String): Boolean =
        scheduleDao.countEntries(monthKey) > 0
}
