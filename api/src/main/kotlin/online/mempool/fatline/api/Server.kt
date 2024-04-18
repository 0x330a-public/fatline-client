package online.mempool.fatline.api

import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import online.mempool.fatline.data.di.AppScope
import retrofit2.OptionalConverterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import javax.inject.Inject
import javax.inject.Named

interface OnboardingClientService {
    @GET("/") suspend fun checkRegistration(@Header("fid") fid: Long): Response<String?>
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
    OnboardingClientService
    // other server impl here also
{

    private val httpClient = OkHttpClient.Builder()
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

    override suspend fun checkRegistration(fid: Long): Response<String?> = onboardFactory.checkRegistration(fid)

}