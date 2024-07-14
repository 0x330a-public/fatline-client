package online.mempool.fatline.api

import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import online.mempool.fatline.data.Profile
import online.mempool.fatline.data.di.AppScope
import retrofit2.OptionalConverterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named


interface FatlineClientService: OnboardingClientService, ProfileClientService

interface OnboardingClientService {
    // Checks the me profile with the stored fid header
    @GET("/profile/me") suspend fun getProfile(): Response<Profile>
    // Checks the registration with temporary fid header, uses profile endpoint for convenience / quick load?
    @GET("/profile/me") suspend fun checkRegistration(): Response<Profile>
}

interface ProfileClientService {
    /** Get a specific user's profile */
    @GET("/profile/{fid}") suspend fun getProfile(@Path("fid") fid: Long): Profile?
    /** Get a specific user's list of follows aka people that user {fid} is FOLLOWED BY */
    @GET("/profile/{fid}/follows") suspend fun getFollows(@Path("fid") fid: Long): List<Profile>?
    /** Get a specific user's list of following aka people that user {fid} is FOLLOWING */
    @GET("/profile/{fid}/following") suspend fun getFollowing(@Path("fid") fid: Long): List<Profile>?
    /** Post specific farcaster messages signed by a valid fid to the server, to be forwarded to the network */
    @POST("/submit_messages") suspend fun postUpdates(@Body serializedUpdates: Messages): Response<Unit>
}

@ExperimentalUnsignedTypes
@Serializable
data class Messages(val updates: List<UByteArray>)

const val SERVER_HTTP_URL = "Server_Named_HttpUrl"
const val AUTH_INTERCEPTOR = "Auth_Interceptor"

const val EXTRA_DATA_HEADER = "extra_sig_data"

@ContributesBinding(AppScope::class, boundType = FatlineClientService::class)
class FatlineClient @Inject constructor(
    @Named(SERVER_HTTP_URL) url: HttpUrl,
    @Named(AUTH_INTERCEPTOR) authenticationInterceptor: Interceptor,
): FatlineClientService {

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authenticationInterceptor)
        // other parameters
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(OptionalConverterFactory.create())
        .addConverterFactory(Json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .client(httpClient)
        .baseUrl(url)
        .build()

    private val onboardFactory = retrofit.create(OnboardingClientService::class.java)
    private val profileFactory = retrofit.create(ProfileClientService::class.java)

    override suspend fun getProfile(): Response<Profile> = onboardFactory.getProfile()
    override suspend fun checkRegistration(): Response<Profile> = onboardFactory.checkRegistration()

    override suspend fun getProfile(fid: Long): Profile? = profileFactory.getProfile(fid)
    override suspend fun getFollows(fid: Long): List<Profile>? = profileFactory.getFollows(fid)
    override suspend fun getFollowing(fid: Long): List<Profile>? = profileFactory.getFollowing(fid)
    override suspend fun postUpdates(serializedUpdates: Messages) = profileFactory.postUpdates(serializedUpdates)
}