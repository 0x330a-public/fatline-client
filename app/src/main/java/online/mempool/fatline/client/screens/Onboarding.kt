@file:SuppressLint("ComposableNaming")
@file:OptIn(ExperimentalStdlibApi::class)

package online.mempool.fatline.client.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import online.mempool.fatline.client.ui.theme.FatlineTheme
import online.mempool.fatline.client.ui.theme.LGE_SPACING
import online.mempool.fatline.client.ui.theme.MED_PLUS_SPACING
import online.mempool.fatline.client.ui.theme.MED_SPACING
import online.mempool.fatline.client.ui.theme.MonospaceRegular
import online.mempool.fatline.client.ui.theme.XL_SPACING
import online.mempool.fatline.data.di.AppScope
import kotlin.random.Random

@Parcelize
data object OnboardingScreen: Screen {
    sealed interface Event: CircuitUiEvent {
        data class Copy(val data: String): Event
        data object OpenUrl: Event
    }
    data class State(
        val publicKey: ByteArray?,
        val eventSink: (Event) -> Unit): CircuitUiState {
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

        LaunchedEffect(Unit) {
            while (isActive) {
                if (userRepository.performRegistrationRequest()) {
                    // navigate to main and set state
                    navigator.resetRoot(ProfileScreen(null))
                    Toast.makeText(context, "Registered", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Not Registered yet...", Toast.LENGTH_LONG).show()
                }
                delay(5_000)
            }
        }

        val clipboard = LocalClipboardManager.current

        return OnboardingScreen.State(userRepository.publicKeyFor(userRepository.selectedKeyIndex)) { event ->
            when (event) {
                is OnboardingScreen.Event.Copy -> {
                    // handle when event for copy in an actual better way and don't just use context
                    clipboard.setText(AnnotatedString(event.data))
                    Toast.makeText(context, "Copied the public key", Toast.LENGTH_LONG).show()
                }
                is OnboardingScreen.Event.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://terminex.mempool.online"))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(null, "Couldn't launch web browser intent")
                    }
                }
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                style = MaterialTheme.typography.headlineMedium,
                text = stringResource(R.string.onboarding_title),
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
                    Icon(painter = painterResource(id = R.drawable.baseline_content_copy_24), contentDescription = stringResource(id = R.string.copy_description))
                }
                Spacer(modifier = Modifier.height(64.dp))
                val annotatedString = AnnotatedString(
                    "Register your public key at\n" +
                        "https://terminex.mempool.online",
                    spanStyle = SpanStyle(color = MaterialTheme.colorScheme.primary),
                    paragraphStyle = ParagraphStyle(textAlign = TextAlign.Center)
                )

                val onClick: ()->Unit = {
                    eventSink(OnboardingScreen.Event.OpenUrl)
                }

                ClickableText(annotatedString,onClick = {onClick()})
                IconButton(
                    onClick = onClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Show indeterminate progress indicator
                Spacer(modifier = Modifier.height(64.dp))
                CircularProgressIndicator()
                Text(
                    text = stringResource(id = R.string.waiting_for_registration),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun PreviewOnboarding() {

    val bytes = Random.Default.nextBytes(32)

    FatlineTheme {
        Onboarding(
            state = OnboardingScreen.State(
                publicKey = bytes,
                eventSink = {}
            )
        )
    }
}