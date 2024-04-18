package online.mempool.fatline.api

import com.squareup.anvil.annotations.ContributesBinding
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import online.mempool.fatline.data.di.AppScope
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Named

interface OnboardingServer {
    @GET("/")
    suspend fun checkRegistration(pubKey: ByteArray, fid: Long): Response<Any>
}

const val SERVER_HTTP_URL = "Server_Named_HttpUrl"
const val AUTH_INTERCEPTOR = "Auth_Interceptor"
const val FID_INTERCEPTOR = "Fid_Interceptor"

const val FID_HEADER = "fid"
const val EXTRA_DATA_HEADER = "extra_sig_data"

@ContributesBinding(AppScope::class, boundType = OnboardingServer::class)
class FatlineServer @Inject constructor(
    @Named(SERVER_HTTP_URL) url: HttpUrl,
    @Named(AUTH_INTERCEPTOR) authenticationInterceptor: Interceptor,
    @Named(FID_INTERCEPTOR) fidInterceptor: Interceptor,
):
    OnboardingServer
    // other server impl here also
{

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(fidInterceptor)
        .addInterceptor(authenticationInterceptor)
        // other parameters
        .build()

    private val retrofit = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(url)
        .build()

    private val onboardFactory = retrofit.create(OnboardingServer::class.java)

    override suspend fun checkRegistration(pubKey: ByteArray, fid: Long): Response<Any> =
        onboardFactory.checkRegistration(pubKey, fid)

}