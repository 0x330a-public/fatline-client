package online.mempool.fatline.data.crypto

import com.ionspin.kotlin.crypto.LibsodiumInitializer
import com.ionspin.kotlin.crypto.kdf.Kdf
import com.ionspin.kotlin.crypto.signature.Signature.seedKeypair


suspend fun initializeSodium() = LibsodiumInitializer.initialize()

@OptIn(ExperimentalUnsignedTypes::class)
/**
 * Generate the master key to be used for deriving fid-specific signing keys
 */
fun generateMasterKey(): ByteArray {
    return Kdf.keygen().toByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
/**
 * Derive a fid-specific ed25519 key from a master key, using KDF derive then using that key as the seed for the Signature keygen
 */
fun UByteArray.deriveFidKey(fid: UInt, generation: Long = 0L): UByteArray {
    val context = if (generation == 0L) "fatline*" else throw Exception("future generations unimplemented")
    val derived = Kdf.deriveFromKey(fid, 32, context,this)
    return seedKeypair(derived).secretKey
}