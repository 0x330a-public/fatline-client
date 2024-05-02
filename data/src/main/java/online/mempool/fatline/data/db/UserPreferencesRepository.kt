package online.mempool.fatline.data.db

interface UserPreferencesRepository {
    fun currentFid(): Long?
    fun setCurrentFid(fid: Long)
}