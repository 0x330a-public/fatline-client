package online.mempool.fatline.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.NavigatorDefaults
import com.slack.circuit.foundation.rememberCircuitNavigator
import online.mempool.fatline.client.di.AppComponent
import online.mempool.fatline.client.screens.OnboardingScreen
import online.mempool.fatline.client.screens.ProfileScreen
import online.mempool.fatline.client.ui.theme.FatlineTheme

class MainActivity : ComponentActivity() {


    private val appComponent by lazy {
        AppComponent.get(applicationContext)
    }

    private val circuit by lazy {
        Circuit.Builder()
            .setDefaultNavDecoration(NavigatorDefaults.EmptyDecoration)
            .addPresenterFactories(appComponent.presenterFactories())
            .addUiFactories(appComponent.uiFactories())
            .build()
    }

    private fun getInitialScreen() =
        appComponent.userFidProvider().get()?.let { fid ->
            ProfileScreen(fid)
        } ?: OnboardingScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FatlineTheme {
                CircuitCompositionLocals(circuit) {
                    val backstack = rememberSaveableBackStack(root = getInitialScreen())
                    val navigator = rememberCircuitNavigator(backstack)
                    NavigableCircuitContent(navigator = navigator, backStack = backstack)
                }
            }
        }
    }
}