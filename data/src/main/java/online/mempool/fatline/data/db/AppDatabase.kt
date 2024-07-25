package online.mempool.fatline.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import online.mempool.fatline.data.IndexedSigner
import online.mempool.fatline.data.Profile
import online.mempool.fatline.data.ProfileFollow

@Database(
    entities = [Profile::class, IndexedSigner::class, ProfileFollow::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ]
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun signerDao(): SignerDao
}