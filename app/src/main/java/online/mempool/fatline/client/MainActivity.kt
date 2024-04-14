package online.mempool.fatline.client

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.Navigator
import com.slack.circuit.foundation.rememberCircuitNavigator
import io.lktk.NativeBLAKE3
import online.mempool.fatline.client.di.AppComponent
import online.mempool.fatline.client.onboarding.OnboardingScreen
import online.mempool.fatline.client.ui.theme.FatlineTheme

class MainActivity : ComponentActivity() {

    // TODO: determine whether the onboarding is needed or not based on saved state
    private fun getInitialScreen() = OnboardingScreen

    private val appComponent by lazy {
        AppComponent.get(applicationContext)
    }

    private val circuit by lazy {
        Circuit.Builder()
            .addPresenterFactories(appComponent.presenterFactories())
            .addUiFactories(appComponent.uiFactories())
            .build()
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    FatlineTheme {
        // A surface container using the 'background' color from the theme
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("") })
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.background
            ) {
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun PreviewMainContent() {
    MainContent()
}