package online.mempool.fatline.client.di

import dagger.Module
import dagger.Provides
import online.mempool.fatline.data.crypto.SecretKeyProvider
import online.mempool.fatline.data.crypto.Signer

@Module
object CryptoModule {

    @Provides
    fun secretKeyProvider(): SecretKeyProvider {
        TODO()
    }

    @Provides
    fun provideSigner(secretKeyProvider: SecretKeyProvider) {
        Signer(secretKeyProvider.getSecretKey())
    }

}