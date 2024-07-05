package online.mempool.fatline.client.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import online.mempool.fatline.api.UserRepository
import online.mempool.fatline.data.db.UserPreferencesRepository
import online.mempool.fatline.data.di.AppScope
import javax.inject.Named
import javax.inject.Provider

@ContributesTo(AppScope::class)
@Module
class PrefsModule(private val userPrefs: UserPreferencesRepository) {

    companion object {
        const val USER_FID = "PrefsModule_USER_FID"
    }

    @Provides
    fun provideUserPrefs() = userPrefs

    @Provides @Named(USER_FID)
    fun provideCurrentUserProvider(userRepository: UserRepository) = runBlocking {
        userRepository.currentFid()
    }

}