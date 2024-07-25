package online.mempool.fatline.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import online.mempool.fatline.data.FidAndTarget
import online.mempool.fatline.data.Profile
import online.mempool.fatline.data.ProfileFollow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile")
    suspend fun getAll(): List<Profile>

    @Upsert
    suspend fun insert(vararg profiles: Profile)

    @Query("SELECT * from profile WHERE fid = :fid")
    fun getUserFlow(fid: Long): Flow<Profile>

    @Query("SELECT * from profile WHERE fid = :fid")
    suspend fun getUser(fid: Long): Profile?

    @Query("SELECT * from profile WHERE profile.fid IN (SELECT links.fid from links WHERE links.target = :fid ORDER BY timestampSeconds DESC)")
    fun getFollowsFlow(fid: Long): Flow<List<Profile>>

    @Query("SELECT * from profile WHERE profile.fid IN (SELECT links.target from links WHERE links.fid = :fid ORDER BY timestampSeconds DESC)")
    fun getFollowingFlow(fid: Long): Flow<List<Profile>>

    @Upsert(entity = ProfileFollow::class)
    fun insertFollows(values: List<FidAndTarget>)

    @Query("DELETE FROM links WHERE fid = :fid")
    fun removeFollowing(fid: Long)

    @Query("DELETE FROM links WHERE target = :fid")
    fun removeFollowers(fid: Long)

    @Transaction
    fun removeAndUpdateFollowing(fid: Long, fidAndTargets: List<FidAndTarget>) {
        removeFollowing(fid)
        insertFollows(fidAndTargets)
    }

    @Transaction
    fun removeAndUpdateFollowers(fid: Long, fidAndTargets: List<FidAndTarget>) {
        removeFollowers(fid)
        insertFollows(fidAndTargets)
    }

}