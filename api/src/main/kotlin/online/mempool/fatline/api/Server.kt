package online.mempool.fatline.api

import com.squareup.anvil.annotations.ContributesBinding
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
import retrofit2.http.GET
import retrofit2.http.Header
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

interface OnboardingClientService {
    // Checks the me profile with the stored fid header
    @GET("/profile/me") suspend fun getProfile(): Response<Profile>
    // Checks the registration with temporary fid header, uses profile endpoint for convenience / quick load?
    @GET("/profile/me") suspend fun checkRegistration(@Header("fid") fid: Long): Response<Profile>
}

const val SERVER_HTTP_URL = "Server_Named_HttpUrl"
const val AUTH_INTERCEPTOR = "Auth_Interceptor"
const val FID_INTERCEPTOR = "Fid_Interceptor"

const val FID_HEADER = "fid"
const val EXTRA_DATA_HEADER = "extra_sig_data"

@ContributesBinding(AppScope::class, boundType = OnboardingClientService::class)
class FatlineClient @Inject constructor(
    @Named(SERVER_HTTP_URL) url: HttpUrl,
    @Named(AUTH_INTERCEPTOR) authenticationInterceptor: Interceptor,
    @Named(FID_INTERCEPTOR) fidInterceptor: Interceptor,
):
    // TODO: interface by delegation
    OnboardingClientService
    // other server impl here also
{

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(fidInterceptor)
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

    override suspend fun getProfile(): Response<Profile> = onboardFactory.getProfile()
    override suspend fun checkRegistration(fid: Long): Response<Profile> = onboardFactory.checkRegistration(fid)

}