package online.mempool.fatline.api

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import online.mempool.fatline.data.crypto.Signer
import online.mempool.fatline.data.di.AppScope
import javax.inject.Named
import javax.inject.Provider

class UserRepository(
    signer: Signer,
    private val onboardingServerProvider: Provider<OnboardingClientService>
) {

    private val onboardingServer by lazy {
        onboardingServerProvider.get()
    }

    private val coroutineContext = Dispatchers.IO + SupervisorJob()

    val publicKey = signer.publicKey
    var fid: Long? = null

    // maybe move this in future
    /**
     * Perform a check for registration status for the fid
     * checking if the internal signer's public key is registered against the fid
     * (server returns 200 if there is an on-chain event registration or bad request / unauthorized / internal server error otherwise)
     */
    suspend fun performRegistrationRequest(fid: Long) = withContext(coroutineContext) {
        onboardingServer.checkRegistration(fid)
            // on result probably store fid if success
            .let { response ->
                if (response.isSuccessful) {
                    // do actual store of user here
                    this@UserRepository.fid = fid
                    // fixme
                    true
                } else false
            }
    }
}

@Module
@ContributesTo(AppScope::class)
class UserRepoModule {
    @Provides
    fun provideUserRepository(signer: Signer, server: Provider<OnboardingClientService>) = UserRepository(signer, server)

    @Provides
    @Named(FID_INTERCEPTOR)
    fun provideFidInterceptor(userRepository: UserRepository) = Interceptor { chain ->
        // add confirmed or pending fid?
        val fid = userRepository.fid
        val hasHeader = !chain.request().header(FID_HEADER).isNullOrEmpty()
        val newReq = chain.request().newBuilder()
        // do our actual saved fid
        if (fid != null && !hasHeader) {
            newReq.header(FID_HEADER, fid.toString())
        }
        chain.proceed(newReq.build())
    }

}