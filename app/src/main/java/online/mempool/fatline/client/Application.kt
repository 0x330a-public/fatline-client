package online.mempool.fatline.client

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.runBlocking
import online.mempool.fatline.client.di.AppComponent
import online.mempool.fatline.data.di.CryptoModule
import online.mempool.fatline.client.di.DaggerAppComponent
import online.mempool.fatline.data.crypto.SecretKeyProvider
import online.mempool.fatline.data.crypto.generateSigningKey
import online.mempool.fatline.data.crypto.initializeSodium

/**
 * Our base application, to provide dependency injection and setup
 */
class Application: Application() {

    companion object {
        private const val APP_COMPONENT = "Application_AppComponent"
    }

    private val appComponent by lazy {
        DaggerAppComponent.builder()
            .cryptoModule(CryptoModule(getSecretKeyProvider()))
            .build()
    }

    override fun getSystemService(name: String): Any {
        if (name == APP_COMPONENT) return appComponent
        return super.getSystemService(name)
    }

    override fun getSystemServiceName(serviceClass: Class<*>): String? {
        if (serviceClass == AppComponent::class.java) return APP_COMPONENT
        return super.getSystemServiceName(serviceClass)
    }

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            initializeSodium()
        }
    }

    private fun getSecretKeyProvider(): SecretKeyProvider = AndroidSecretKeyProvider(this)

}

class AndroidSecretKeyProvider(context: Context): SecretKeyProvider {
    companion object {
        private const val KEY_ALIAS = "fatline_key"
        private const val SIGNING_KEY = "signing_key"
    }

    private val masterKey = MasterKey.Builder(context, KEY_ALIAS)
        .setRequestStrongBoxBacked(true)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "fatline-key-prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSecretKey(): ByteArray {
        if (!prefs.contains(SIGNING_KEY)) {
            prefs.edit {
                val key = generateSigningKey()
                putString(SIGNING_KEY, key.toHexString())
            }
        }
        val encoded = prefs.getString(SIGNING_KEY, null)!!
        return encoded.hexToByteArray()
    }

}