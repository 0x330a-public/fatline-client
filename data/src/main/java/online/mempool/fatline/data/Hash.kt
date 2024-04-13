package online.mempool.fatline.data

import io.lktk.NativeBLAKE3

fun hash(data: ByteArray, length: Int = 20): ByteArray {
    val hasher = NativeBLAKE3()
    hasher.initDefault()
    hasher.update(data)
    val toReturn = hasher.getOutput(length)
    hasher.close()
    return toReturn
}