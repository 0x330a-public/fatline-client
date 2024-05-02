package online.mempool.fatline.client.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import online.mempool.fatline.data.db.UserPreferencesRepository
import online.mempool.fatline.data.di.AppScope

@ContributesTo(AppScope::class)
@Module
class PrefsModule(private val userPrefs: UserPreferencesRepository) {

    @Provides
    fun provideUserPrefs() = userPrefs

}