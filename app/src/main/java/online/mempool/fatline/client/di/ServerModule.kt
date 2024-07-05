package online.mempool.fatline.client.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl.Companion.toHttpUrl
import online.mempool.fatline.api.SERVER_HTTP_URL
import online.mempool.fatline.client.BuildConfig
import online.mempool.fatline.data.di.AppScope
import javax.inject.Named

@Module
@ContributesTo(AppScope::class)
class ServerModule {

    @Provides
    @Named(SERVER_HTTP_URL)
    fun provideUrl() = BuildConfig.SERVER_URL.toHttpUrl()

}