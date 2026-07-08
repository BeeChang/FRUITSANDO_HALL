package example.yf.fruit_hall.data.tray.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tray_items",
    foreignKeys = [
        ForeignKey(
            entity = TrayEntity::class,
            parentColumns = ["id"],
            childColumns = ["trayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SnackTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["snackTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trayId"), Index("snackTypeId"), Index(value = ["trayId", "snackTypeId"], unique = true)]
)
data class TrayItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trayId: Long,
    val snackTypeId: Long,
    val exactQty: Int? = null,
    val roughSize: String? = null // RoughSize.name: "S"/"M"/"L"
)
