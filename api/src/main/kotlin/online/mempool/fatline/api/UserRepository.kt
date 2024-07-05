package online.mempool.fatline.api

import android.util.Log
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import online.mempool.fatline.data.IndexedSigner
import online.mempool.fatline.data.Profile
import online.mempool.fatline.data.crypto.Signer
import online.mempool.fatline.data.crypto.Signer.Companion.bindSigner
import online.mempool.fatline.data.db.ProfileDao
import online.mempool.fatline.data.db.SignerDao
import online.mempool.fatline.data.db.UserPreferencesRepository
import online.mempool.fatline.data.di.AppScope
import java.util.logging.Logger
import javax.inject.Named
import javax.inject.Provider

class UserRepository(
    private val signer: Signer,
    private val clientProvider: Provider<FatlineClientService>,
    private val profileDao: ProfileDao,
    private val signerDao: SignerDao,
    private val userPrefsRepository: UserPreferencesRepository,
) {

    companion object {
        private const val TAG = "UserRepository"
    }

    private val fatlineClient by lazy {
        clientProvider.get()
    }

    private val context = Dispatchers.IO + SupervisorJob()
    private val scope = CoroutineScope(context)

    var selectedKeyIndex: Long = userPrefsRepository.currentKeyIndex()
        get() = userPrefsRepository.currentKeyIndex()
        set(value) {
            userPrefsRepository.activateKey(value)
            field = value
        }

    suspend fun currentProfile(): Profile? = signerDao.fidForSigner(selectedKeyIndex)?.let {
        profileDao.getUser(it)
    }

    suspend fun currentFid(): Long? = signerDao.fidForSigner(selectedKeyIndex)

    fun publicKeyFor(index: Long) = signer.publicKey(index.toUInt())

    private val updateChannel = Channel<Long>(capacity = 64, BufferOverflow.DROP_OLDEST)

    init {
        scope.launch {
            updateChannel.receiveAsFlow().collectLatest { fid ->
                runCatching {
                    fatlineClient.getProfile(fid)?.let { profile ->
                        profileDao.insert(profile)
                    }
                }.exceptionOrNull()?.let {
                    Log.e(TAG, "Error fetching update for user $fid")
                }
            }
        }
    }

    /**
     * Perform a check for registration status for the fid
     * checking if the internal signer's public key is registered against the fid
     * (server returns 200 if there is an on-chain event registration or bad request / unauthorized / internal server error otherwise)
     */
    suspend fun performRegistrationRequest() = // TODO: add optional key specifier ?
        runCatching {
            withContext(context) {
                fatlineClient.checkRegistration()
                    // on result probably store fid if success
                    .let { response ->
                        if (response.isSuccessful) {
                            response.body()?.let { returnedProfile ->
                                val profileFid = returnedProfile.fid
                                profileDao.insert(returnedProfile)
                                val indexed = IndexedSigner (
                                    keyIndex = selectedKeyIndex,
                                    publicKey = publicKeyFor(selectedKeyIndex),
                                    forFid = profileFid,
                                    isActive = true
                                )
                                signerDao.insert(indexed)
                            }
                        }
                        response.isSuccessful
                    }
            }
        }.getOrElse {
            Log.e(TAG, "Failed to check registration", it)
            false
        }

    fun user(userFid: Long?) = runBlocking {
        val fetchFid = withContext(context) {
            userFid ?: currentFid()!!
        }

        profileDao.getUserFlow(fetchFid)
            .onStart {
                updateChannel.send(fetchFid)
            }
    }

    fun follows(userFid: Long?, refreshes: Flow<Unit>) = runBlocking {
        val fetchFid = withContext(context) {
            userFid ?: currentFid()!!
        }

        refreshes.onStart { emit(Unit) }.map {
            withContext(context) {
                runCatching {
                    fatlineClient.getFollows(fetchFid).orEmpty()
                }.getOrElse { emptyList() }
            }
        }
    }

    fun following(userFid: Long? , refreshes: Flow<Unit>) = runBlocking {
        val fetchFid = withContext(context) {
            userFid ?: currentFid()!!
        }

        refreshes.onStart { emit(Unit) }.map {
            withContext(context) {
                runCatching {
                    fatlineClient.getFollowing(fetchFid).orEmpty()
                }.getOrElse { emptyList() }
            }
        }
    }

}

@Module
@ContributesTo(AppScope::class)
class UserRepoModule {
    @Provides
    fun provideUserRepository(signer: Signer,
                              server: Provider<FatlineClientService>,
                              profileDao: ProfileDao,
                              signerDao: SignerDao,
                              userPrefs: UserPreferencesRepository,
                              ) = UserRepository(signer, server, profileDao, signerDao, userPrefs)

    @Provides
    @Named(AUTH_INTERCEPTOR)
    fun provideAuthInterceptor(signer: Signer, userRepository: UserRepository) = Interceptor { chain ->
        val prevRequest = chain.request()
        val extraData = prevRequest.header(EXTRA_DATA_HEADER)
        // it is an error to not have a fid already here from a previous interceptor
        // maybe do something with this?
        val newRequest = chain.request().newBuilder()
            .bindSigner(signer, userRepository.selectedKeyIndex.toUInt(), extraData)
        chain.proceed(newRequest.build())
    }

}