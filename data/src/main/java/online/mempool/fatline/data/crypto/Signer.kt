@file:OptIn(ExperimentalStdlibApi::class)

package online.mempool.fatline.data.crypto

import com.ionspin.kotlin.crypto.signature.Signature
import okhttp3.Request
import online.mempool.fatline.data.DEFAULT_HASH_LENGTH
import online.mempool.fatline.data.Hash

@OptIn(ExperimentalUnsignedTypes::class)
class Signer(secretKeyBytes: ByteArray) {

    companion object {
        // attach timestamp header, signature, message etc
        /**
         * Adds authentication headers to the request:
         *
         */
        fun Request.Builder.bindSigner(signer: Signer, keyIndex: UInt, extraData: String?): Request.Builder {
            // constants for headers
            // required headers: pub_hex, timestamp, sig, fid
            // optional header: extra_sig_data, maybe use this as route specific signature verification instead of overall?
            // wip implementation: H(pub_key (not hex) || timestamp || [optional: extra_sig_data]) -> should match sig for pub key
            val pubKey = signer.publicKey(keyIndex)
            val pubKeyHex = pubKey.toHexString()
            header("key_hex", pubKeyHex)
            val timestamp = System.currentTimeMillis().toString()
            header("timestamp", timestamp)
            val extraDataEncoded = extraData?.encodeToByteArray() ?: byteArrayOf()
            if (extraDataEncoded.isNotEmpty()) {
                header("extra_sig_data_hex", extraDataEncoded.toHexString())
            }

            val sigData = Hash.hash(pubKey + timestamp.encodeToByteArray() + extraDataEncoded)

            // we should probably explode here if we don't successfully sign the data
            header("sig", signer.signed(keyIndex, sigData).getOrNull()!!.toHexString())

            return this
        }
    }

    sealed class Failure(message: String): Throwable(message) {
        data object InvalidHashLength: Failure("Hash was not $DEFAULT_HASH_LENGTH unsigned bytes long") {
            private fun readResolve(): Any = InvalidHashLength
        }
    }

    private val masterKey = secretKeyBytes.asUByteArray()

    /**
     * @param keyIndex the key index to generate a deterministic key for
     */
    private fun derived(keyIndex: UInt): UByteArray {
        // TODO: maybe use generation hashed with kdf context or sth
        return masterKey.deriveFidKey(keyIndex)
    }

    fun publicKey(keyIndex: UInt): ByteArray {
        val derived = derived(keyIndex)
        return Signature.ed25519SkToPk(derived).toByteArray()
    }

    /**
     * Sign a hash, assuming a hash length of the truncated Farcaster default (20 bytes)
     * @return Result of successful sign or failure if the byte array was not the expected length
     */
    fun signed(keyIndex: UInt, bytes: ByteArray): Result<ByteArray> {
        val asUnsigned = bytes.asUByteArray()
        if (asUnsigned.size != DEFAULT_HASH_LENGTH) return Result.failure(Failure.InvalidHashLength)
        return Result.success(Signature.detached(asUnsigned, derived(keyIndex)).toByteArray())
    }
}