package online.mempool.fatline.data.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import online.mempool.fatline.data.crypto.SecretKeyProvider
import online.mempool.fatline.data.crypto.Signer

@Module
@ContributesTo(AppScope::class)
class CryptoModule(private val secretKeyProvider: SecretKeyProvider) {

    @Provides
    fun provideSigner() =
        Signer(secretKeyProvider.getMasterKey())

}