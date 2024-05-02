package online.mempool.fatline.data.crypto

interface SecretKeyProvider {
    fun getMasterKey(): ByteArray
}