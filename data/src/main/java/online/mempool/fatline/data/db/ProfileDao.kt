package online.mempool.fatline.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import online.mempool.fatline.data.Profile

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile")
    suspend fun getAll(): List<Profile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg profiles: Profile)

    @Query("SELECT * from profile WHERE fid = :fid")
    fun getUserFlow(fid: Long): Flow<Profile>

    @Query("SELECT * from profile WHERE fid = :fid")
    suspend fun getUser(fid: Long): Profile?

}