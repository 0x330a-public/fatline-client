package online.mempool.fatline.data

const val DEFAULT_HASH_LENGTH = 20

object Hash {
    init {
        System.loadLibrary("jni_lib")
    }
    external fun hash(data: ByteArray): ByteArray
}