package online.mempool.fatline.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import online.mempool.fatline.data.Profile

@Database(entities = [Profile::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun profileDao(): ProfileDao
}