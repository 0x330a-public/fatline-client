package online.mempool.fatline.data

data class Profile(
    val fid: Long,
    val username: String?,
    val displayName: String?,
    val profilePicture: String?,
    val bio: String?,
    val url: String?
) {
    val displayTag: String
        get() = "@${username ?: ("~$fid")}"
}