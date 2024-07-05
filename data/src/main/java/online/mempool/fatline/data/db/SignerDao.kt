package online.mempool.fatline.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import online.mempool.fatline.data.IndexedSigner

@Dao
interface SignerDao {

    @Query("SELECT COUNT(*) from indexedsigner")
    suspend fun signerCount(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signer: IndexedSigner)

    @Query("SELECT forFid FROM indexedsigner WHERE keyIndex = :keyIndex")
    suspend fun fidForSigner(keyIndex: Long): Long?

}