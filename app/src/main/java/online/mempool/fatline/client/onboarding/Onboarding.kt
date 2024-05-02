@file:SuppressLint("ComposableNaming")
@file:OptIn(ExperimentalStdlibApi::class)

package online.mempool.fatline.client.onboarding

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.util.fastJoinToString
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.parcelize.Parcelize
import online.mempool.fatline.api.UserRepository
import online.mempool.fatline.client.R
import online.mempool.fatline.client.ui.theme.LGE_SPACING
import online.mempool.fatline.client.ui.theme.MED_PLUS_SPACING
import online.mempool.fatline.client.ui.theme.MED_SPACING
import online.mempool.fatline.client.ui.theme.MonospaceRegular
import online.mempool.fatline.client.ui.theme.XL_SPACING
import online.mempool.fatline.data.di.AppScope

sealed interface RegistrationState {
    data object NotRegistering: RegistrationState
    data object Registering: RegistrationState
    data class Error(val exception: Exception): RegistrationState
}

@Parcelize
data object OnboardingScreen: Screen {
    sealed interface Event: CircuitUiEvent {
        data class Copy(val data: String): Event
        data class UpdateFid(val fid: Long): Event
    }
    data class State(val publicKey: ByteArray?, val registrationState: RegistrationState, val eventSink: (Event) -> Unit): CircuitUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as State

            return publicKey.contentEquals(other.publicKey)
        }

        override fun hashCode(): Int {
            return publicKey.contentHashCode()
        }
    }
}


class OnboardingPresenter @AssistedInject constructor(@Assisted private val navigator: Navigator,
                                                      private val userRepository: UserRepository): Presenter<OnboardingScreen.State> {
    @Composable
    override fun present(): OnboardingScreen.State {
        val context = LocalContext.current

        var checkedFid: Long? by remember { mutableStateOf(null) }

        var registrationState by remember { mutableStateOf(RegistrationState.NotRegistering) }

        LaunchedEffect(checkedFid) {
            val fid = checkedFid ?: run {
                if (registrationState != RegistrationState.NotRegistering) {
                    registrationState = RegistrationState.NotRegistering
                }
                return@LaunchedEffect
            }
            while (isActive) {
                if (userRepository.performRegistrationRequest(fid)) {
                    // navigate to main and set state
                    Toast.makeText(context, "Registered", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Not Registered yet...", Toast.LENGTH_LONG).show()
                }
                delay(5_000)
            }
        }

        val clipboard = LocalClipboardManager.current

        return OnboardingScreen.State(checkedFid?.toUInt()?.let(userRepository::publicKeyFor), registrationState) { event ->
            when (event) {
                is OnboardingScreen.Event.Copy -> {
                    // handle when event for copy in an actual better way and don't just use context
                    clipboard.setText(AnnotatedString(event.data))
                    Toast.makeText(context, "Copied the pubkey", Toast.LENGTH_LONG).show()
                }

                is OnboardingScreen.Event.UpdateFid -> checkedFid = event.fid
            }
        }
    }

    @CircuitInject(OnboardingScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): OnboardingPresenter
    }
}

@CircuitInject(OnboardingScreen::class, AppScope::class)
@Composable
fun Onboarding(state: OnboardingScreen.State, modifier: Modifier = Modifier) {

    val eventSink = state.eventSink
    var checkFid by remember { mutableStateOf<Long?>(null) }

    Scaffold { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                style = MaterialTheme.typography.headlineMedium,
                text = stringResource(R.string.onboardingTitle),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    start = MED_PLUS_SPACING,
                    end = MED_PLUS_SPACING,
                    top = LGE_SPACING,
                    bottom = MED_SPACING
                )
            )

            if (state.publicKey != null) {
                Text(
                    state.publicKey.toHexString().chunked(4).chunked(4)
                        .fastJoinToString("\n") { chunk -> chunk.fastJoinToString(" ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = XL_SPACING, vertical = MED_SPACING),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = MonospaceRegular)
                IconButton(onClick = { eventSink(OnboardingScreen.Event.Copy(state.publicKey.toHexString())) }) {
                    Icon(painter = painterResource(id = R.drawable.baseline_content_copy_24), contentDescription = stringResource(id = R.string.copyDescription))
                }
            }

            TextField(value = checkFid?.toString() ?: "", onValueChange = { value: String ->
                value.toLongOrNull()?.let { fidValue ->
                    checkFid = fidValue
                    eventSink(OnboardingScreen.Event.UpdateFid(fidValue))
                }
            }, modifier = Modifier.fillMaxWidth(0.4f))
        }
    }
}