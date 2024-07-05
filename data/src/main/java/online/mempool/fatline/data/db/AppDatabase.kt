package online.mempool.fatline.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import online.mempool.fatline.data.Profile
import online.mempool.fatline.data.IndexedSigner

@Database(entities = [Profile::class, IndexedSigner::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun signerDao(): SignerDao
}