package online.mempool.fatline.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class IndexedSigner(
    @PrimaryKey
    val keyIndex: Long,
    val publicKey: ByteArray,
    val forFid: Long,
    val isActive: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IndexedSigner

        if (keyIndex != other.keyIndex) return false
        if (!publicKey.contentEquals(other.publicKey)) return false
        if (forFid != other.forFid) return false
        if (isActive != other.isActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyIndex.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + forFid.hashCode()
        result = 31 * result + isActive.hashCode()
        return result
    }
}