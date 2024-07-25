package online.mempool.fatline.data.db

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import online.mempool.fatline.proto.LinkIndex
import java.io.InputStream
import java.io.OutputStream

object LinkIndexSerializer: Serializer<LinkIndex> {
    override val defaultValue: LinkIndex = LinkIndex.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): LinkIndex {
        try {
            return LinkIndex.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: LinkIndex,
        output: OutputStream
    ) = t.writeTo(output)
}

val Context.linkIndexDataStore: DataStore<LinkIndex> by dataStore(
    fileName = "link_index.pb",
    serializer = LinkIndexSerializer
)