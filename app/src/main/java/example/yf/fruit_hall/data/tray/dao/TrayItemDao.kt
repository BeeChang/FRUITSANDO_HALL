package example.yf.fruit_hall.data.tray.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import example.yf.fruit_hall.data.tray.entity.TrayItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrayItemDao {
    // ORDER BY id: 판에 담은 실제 순서(드래그한 순서)를 그대로 보존하기 위함
    @Query("SELECT * FROM tray_items ORDER BY id")
    fun observeAll(): Flow<List<TrayItemEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insert(item: TrayItemEntity): Long

    // 사이즈만 바꿀 때는 UPDATE로 처리 — insert(REPLACE)를 쓰면 행이 삭제 후 재생성되어
    // id가 바뀌면서 담긴 순서 맨 뒤로 밀려나는 부작용이 있다
    @Query("UPDATE tray_items SET roughSize = :roughSize WHERE trayId = :trayId AND snackTypeId = :snackTypeId")
    suspend fun updateRoughSize(trayId: Long, snackTypeId: Long, roughSize: String?)

    @Delete
    suspend fun delete(item: TrayItemEntity)

    @Query("DELETE FROM tray_items WHERE trayId = :trayId AND snackTypeId = :snackTypeId")
    suspend fun deleteByTrayAndType(trayId: Long, snackTypeId: Long)

    @Query("DELETE FROM tray_items WHERE trayId = :trayId")
    suspend fun deleteByTray(trayId: Long)
}
