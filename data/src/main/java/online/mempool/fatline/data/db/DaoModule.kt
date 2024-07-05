package online.mempool.fatline.data.db

import android.content.Context
import androidx.room.Room
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import online.mempool.fatline.data.di.AppScope

@ContributesTo(AppScope::class)
@Module
class DaoModule(private val context: Context) {

    @Provides
    fun provideAppDatabase() = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "fatline-database"
    ).build()

    @Provides
    fun provideProfileStorage(appDatabase: AppDatabase) = appDatabase.profileDao()

    @Provides
    fun provideSignerStorage(appDatabase: AppDatabase) = appDatabase.signerDao()

}