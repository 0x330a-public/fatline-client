package online.mempool.fatline.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Profile(
    @PrimaryKey
    val fid: Long,
    val username: String?,
    val display_name: String?,
    val profile_picture: String?,
    val bio: String?,
    val url: String?
) {
    val displayTag: String
        get() = "@${username ?: ("~$fid")}"
}