package online.mempool.fatline.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    primaryKeys = ["fid", "target"],
    tableName = "links"
)
data class ProfileFollow(
    val fid: Long,
    val target: Long,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    val timestampSeconds: Long
)

data class FidAndTarget(
    val fid: Long,
    val target: Long
)