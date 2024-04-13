package online.mempool.fatline.data.crypto

import com.ionspin.kotlin.crypto.signature.Signature
import online.mempool.fatline.data.DEFAULT_HASH_LENGTH

@OptIn(ExperimentalUnsignedTypes::class)
class Signer(secretKeyBytes: ByteArray) {

    sealed class Failure(message: String): Throwable(message) {
        data object InvalidHashLength: Failure("Hash was not $DEFAULT_HASH_LENGTH unsigned bytes long") {
            private fun readResolve(): Any = InvalidHashLength
        }
    }

    private val secretKey = secretKeyBytes.asUByteArray()
    val publicKey = Signature.ed25519SkToPk(secretKey).asByteArray()

    /**
     * Sign a hash, assuming a hash length of the truncated Farcaster default (20 bytes)
     * @return Result of successful sign or failure if the byte array was not the expected length
     */
    fun ByteArray.signed(): Result<ByteArray> {
        val asUnsigned = this.asUByteArray()
        if (asUnsigned.size != DEFAULT_HASH_LENGTH) return Result.failure(Failure.InvalidHashLength)
        return Result.success(Signature.detached(asUnsigned, secretKey).asByteArray())
    }

}