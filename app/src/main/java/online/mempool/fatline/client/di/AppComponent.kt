package online.mempool.fatline.client.di

import android.content.Context
import androidx.core.content.getSystemService
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.squareup.anvil.annotations.MergeComponent
import dagger.Component
import online.mempool.fatline.api.UserRepository
import online.mempool.fatline.client.di.PrefsModule.Companion.USER_FID
import online.mempool.fatline.data.di.AppScope
import javax.inject.Named
import javax.inject.Provider

@MergeComponent(AppScope::class)
interface AppComponent {
    companion object {
        fun get(context: Context): AppComponent =
            context.getSystemService(AppComponent::class.java)
    }

    fun presenterFactories(): Set<Presenter.Factory>
    fun uiFactories(): Set<Ui.Factory>
    @Named(USER_FID)
    fun userFidProvider(): Provider<Long?>

}