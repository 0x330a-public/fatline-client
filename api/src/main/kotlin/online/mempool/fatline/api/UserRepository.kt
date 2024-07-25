package online.mempool.fatline.api

import MessageOuterClass
import MessageOuterClass.FarcasterNetwork.FARCASTER_NETWORK_MAINNET
import MessageOuterClass.HashScheme.HASH_SCHEME_BLAKE3
import MessageOuterClass.SignatureScheme.SIGNATURE_SCHEME_ED25519
import android.util.Log
import androidx.datastore.core.DataStore
import com.google.protobuf.kotlin.toByteString
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import message
import messageData
import okhttp3.Interceptor
import online.mempool.fatline.data.FidAndTarget
import online.mempool.fatline.data.Hash
import online.mempool.fatline.data.IndexedSigner
import online.mempool.fatline.data.Profile
import online.mempool.fatline.data.crypto.Signer
import online.mempool.fatline.data.crypto.Signer.Companion.bindSigner
import online.mempool.fatline.data.db.ProfileDao
import online.mempool.fatline.data.db.SignerDao
import online.mempool.fatline.data.db.UserPreferencesRepository
import online.mempool.fatline.data.di.AppScope
import online.mempool.fatline.data.farcasterEpochNow
import online.mempool.fatline.proto.LinkIndex
import javax.inject.Named
import javax.inject.Provider

enum class FollowDirection {
    FOLLOWING,
    FOLLOWS
}

class UserRepository(
    private val signer: Signer,
    private val clientProvider: Provider<FatlineClientService>,
    private val profileDao: ProfileDao,
    private val signerDao: SignerDao,
    private val userPrefsRepository: UserPreferencesRepository,
    private val linkIndexStorage: DataStore<LinkIndex>
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
        withContext(context) {
            profileDao.getUser(it)
        }
    }

    suspend fun currentFid(): Long? = signerDao.fidForSigner(selectedKeyIndex)

    fun publicKeyFor(index: Long) = signer.publicKey(index.toUInt())

    private val updateChannel = Channel<Long>(capacity = 64, BufferOverflow.DROP_OLDEST)
    private val followUpdateChannel = Channel<Pair<Long,FollowDirection>>(capacity = 2048, BufferOverflow.DROP_OLDEST)

    init {
        scope.launch {
            followUpdateChannel.receiveAsFlow().collect { (fid, direction) ->
                runCatching {
                    val newList = when (direction) {
                        FollowDirection.FOLLOWS -> {
                            fatlineClient.getFollows(fid)
                        }
                        FollowDirection.FOLLOWING -> {
                            fatlineClient.getFollowing(fid)
                        }
                    } ?: return@runCatching
                    profileDao.insert(*newList.toTypedArray())
                    setIndexedFollows(fid)
                    when (direction) {
                        FollowDirection.FOLLOWS -> {
                            profileDao.removeAndUpdateFollowers(fid, newList.map { p ->
                                FidAndTarget(p.fid, fid)
                            })
                        }
                        FollowDirection.FOLLOWING -> {
                            profileDao.removeAndUpdateFollowing(fid, newList.map { p ->
                                FidAndTarget(fid, p.fid)
                            })
                        }
                    }
                }
            }
        }
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

    fun refreshFollowing(fid: Long) {
        followUpdateChannel.trySend(fid to FollowDirection.FOLLOWING)
    }

    fun refreshFollows(fid: Long) {
        followUpdateChannel.trySend(fid to FollowDirection.FOLLOWS)
    }

    suspend fun setIndexedFollows(fid: Long) = withContext(context) {
        linkIndexStorage.updateData { storage ->
            storage.toBuilder()
                .putFidMapping(fid, true)
                .build()
        }
    }

    suspend fun hasIndexedFollows(fid: Long) = withContext(context) {
        linkIndexStorage.data.first().fidMappingMap[fid] == true
    }

    fun follows(userFid: Long?) = runBlocking {
        val fetchFid = withContext(context) {
            userFid ?: currentFid()!!
        }

        profileDao.getFollowsFlow(fetchFid).onStart {
            refreshFollows(fetchFid)
        }.onEach { profiles ->
            Log.d("follows", "New follows list ${profiles}")
        }.filter {
            // do a cheeky gate here until we have indexed follows
            hasIndexedFollows(fetchFid)
        }
    }

    fun following(userFid: Long?) = runBlocking {
        val fetchFid = withContext(context) {
            userFid ?: currentFid()!!
        }

        profileDao.getFollowingFlow(fetchFid).onStart {
            refreshFollowing(fetchFid)
        }.onEach { profiles ->
            Log.d("following", "New following list ${profiles}")
        }.filter {
            hasIndexedFollows(fetchFid)
        }
    }

    private suspend fun insertNewProfiles(profiles: List<Profile>) {
        if (profiles.isNotEmpty()) {
            withContext(context) {
                profileDao.insert(*profiles.toTypedArray())
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun postUpdates(updates: List<MessageOuterClass.UserDataBody>) {
        val currentSignerIndex = selectedKeyIndex.toUInt()
        val pubkey = publicKeyFor(selectedKeyIndex)
        scope.launch(context) {
            val messages = updates.map { update ->
                message {
                    val toHash = messageData {
                        type = MessageOuterClass.MessageType.MESSAGE_TYPE_USER_DATA_ADD
                        fid = currentFid()!!
                        timestamp = farcasterEpochNow()
                        network = FARCASTER_NETWORK_MAINNET
                        userDataBody = update
                    }
                    data = toHash
                    val hashedContent = Hash.hash(toHash.toByteArray())
                    hash = hashedContent.toByteString()
                    hashScheme = HASH_SCHEME_BLAKE3
                    signer = pubkey.toByteString()
                    signatureScheme = SIGNATURE_SCHEME_ED25519
                    signature = this@UserRepository.signer.signed(currentSignerIndex, hashedContent).getOrThrow().toByteString()
                }.toByteArray().toUByteArray()
            }
            val response = fatlineClient.postUpdates(Messages(messages))
            Log.d(TAG, "response was $response")
        }
    }

    suspend fun getUsers(userFids: List<Long>): List<Profile> =
        runCatching {
            withContext(context) {
                userFids.mapNotNull { fid ->
                    profileDao.getUser(fid)
                }
            }
        }.getOrElse { emptyList() }

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
                              linkDataStore: DataStore<LinkIndex>
                              ) = UserRepository(signer, server, profileDao, signerDao, userPrefs, linkDataStore)

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