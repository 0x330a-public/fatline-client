package online.mempool.fatline.data.crypto

import com.ionspin.kotlin.crypto.LibsodiumInitializer
import com.ionspin.kotlin.crypto.signature.Signature.seedKeypair
import com.ionspin.kotlin.crypto.util.LibsodiumRandom


suspend fun initializeSodium() = LibsodiumInitializer.initialize()

@OptIn(ExperimentalUnsignedTypes::class)
fun generateSigningKey(): ByteArray {
    return seedKeypair(LibsodiumRandom.buf(32)).secretKey.asByteArray()
}