package online.mempool.fatline.data

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
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