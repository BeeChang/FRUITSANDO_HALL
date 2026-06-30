package example.yf.fruit_hall.data.schedule.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import example.yf.fruit_hall.data.schedule.entity.ScheduleEntryEntity
import example.yf.fruit_hall.data.schedule.entity.ScheduleMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_metadata WHERE monthKey = :monthKey")
    suspend fun getMetadata(monthKey: String): ScheduleMetadataEntity?

    @Upsert
    suspend fun upsertMetadata(metadata: ScheduleMetadataEntity)

    @Query("DELETE FROM schedule_entry WHERE monthKey = :monthKey")
    suspend fun deleteEntriesByMonth(monthKey: String)

    @Insert
    suspend fun insertEntries(entries: List<ScheduleEntryEntity>)

    @Transaction
    suspend fun replaceMonthData(
        metadata: ScheduleMetadataEntity,
        entries: List<ScheduleEntryEntity>
    ) {
        deleteEntriesByMonth(metadata.monthKey)
        insertEntries(entries)
        upsertMetadata(metadata)
    }

    @Query("SELECT * FROM schedule_entry WHERE monthKey = :monthKey ORDER BY date ASC, personName ASC")
    fun observeEntriesForMonth(monthKey: String): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT COUNT(*) FROM schedule_entry WHERE monthKey = :monthKey")
    suspend fun countEntries(monthKey: String): Int
}
