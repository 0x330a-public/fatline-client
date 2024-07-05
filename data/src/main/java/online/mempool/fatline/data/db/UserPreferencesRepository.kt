package online.mempool.fatline.data.db

interface UserPreferencesRepository {
    fun currentKeyIndex(): Long
    fun activateKey(index: Long)
}