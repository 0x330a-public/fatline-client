package online.mempool.fatline.data.db

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import online.mempool.fatline.data.Profile

@Dao
interface ProfileStorage {

    @Query("SELECT * FROM profile")
    fun getAll(): List<Profile>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg profiles: Profile)

}