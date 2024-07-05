package online.mempool.fatline.client.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.parcelize.Parcelize
import online.mempool.fatline.api.UserRepository
import online.mempool.fatline.data.di.AppScope

@Parcelize
data object EditProfileScreen: Screen {
    sealed interface Event: CircuitUiEvent {
        data object NavigateBack: Event
    }
    data class State(
        val profileState: ProfileLoadingState,
        val eventSink: (Event) -> Unit
    ): CircuitUiState
}

class EditProfilePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val userRepository: UserRepository
): Presenter<EditProfileScreen.State> {

    @Composable
    override fun present(): EditProfileScreen.State {

        val preEdit by produceState<ProfileLoadingState>(initialValue = ProfileLoadingState.Loading, Unit) {
            value = runCatching {
                ProfileLoadingState.Loaded(userRepository.currentProfile()!!, true)
            }.getOrElse { ProfileLoadingState.LoadError(it) }
        }

        return EditProfileScreen.State(
            preEdit
        ) { event ->
            when (event) {
                EditProfileScreen.Event.NavigateBack -> navigator.pop()
            }
        }

    }

    @CircuitInject(EditProfileScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): EditProfilePresenter
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(EditProfileScreen::class, AppScope::class)
@Composable
fun EditProfile(state: EditProfileScreen.State, modifier: Modifier = Modifier) {
    Scaffold(topBar = {
        TopAppBar(title = { /*TODO*/ }, navigationIcon = {
            IconButton(onClick = { state.eventSink(EditProfileScreen.Event.NavigateBack) }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
        })
    }, modifier = modifier) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            /*TODO*/
        }
    }
}