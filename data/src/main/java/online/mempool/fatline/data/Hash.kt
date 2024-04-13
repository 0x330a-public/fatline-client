package online.mempool.fatline.data

import io.lktk.NativeBLAKE3

const val DEFAULT_HASH_LENGTH = 20

/**
 * Hash some data using BLAKE3, returning the resulting bytes
 * @param length The length of the returned hash, or 20 bytes by default
 */
fun ByteArray.hash(length: Int = DEFAULT_HASH_LENGTH): ByteArray {
    val hasher = NativeBLAKE3()
    hasher.initDefault()
    hasher.update(this)
    val toReturn = hasher.getOutput(length)
    hasher.close()
    return toReturn
}