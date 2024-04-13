@file:SuppressLint("ComposableNaming")
package online.mempool.fatline.client.onboarding

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import online.mempool.fatline.data.crypto.Signer
import online.mempool.fatline.data.di.AppScope

@Parcelize
data object OnboardingScreen: Screen {
    data class State(val publicKey: ByteArray): CircuitUiState {
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


@CircuitInject(OnboardingScreen::class, AppScope::class)
@Composable
fun OnboardingPresenter(signer: Signer): OnboardingScreen.State {

    return OnboardingScreen.State(signer.publicKey)
}

@CircuitInject(OnboardingScreen::class, AppScope::class)
@Composable
fun Onboarding(state: OnboardingScreen.State, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Text("Add this public key:\n${state.publicKey}")
    }
}