package online.mempool.fatline.client.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import online.mempool.fatline.client.screens.UserListScreen.Event
import online.mempool.fatline.client.screens.UserListScreen.ListLoadingState
import online.mempool.fatline.client.screens.UserListScreen.State
import online.mempool.fatline.client.ui.ProfileListItem
import online.mempool.fatline.data.Profile
import online.mempool.fatline.data.di.AppScope

@Parcelize
data class UserListScreen(@StringRes val title: Int, val userFids: List<Long>): Screen {
    sealed interface Event: CircuitUiEvent {
        data class NavigateToProfile(val fid: Long): Event
        data object NavigateBack: Event
    }
    sealed class ListLoadingState {
        // list should always have items / default empty
        abstract val items: List<Profile>

        data object Loading: ListLoadingState() { override val items:List<Profile> = emptyList() }
        data class Loaded(override val items: List<Profile>): ListLoadingState()
    }
    data class State(
        @StringRes val title: Int,
        val loadingState: ListLoadingState,
        val eventSink: (Event)->Unit
    ): CircuitUiState
}

class UserListPresenter @AssistedInject constructor(@Assisted private val navigator: Navigator,
                                                    @Assisted private val screen: UserListScreen,
                                                    private val userRepository: UserRepository
): Presenter<State> {
    @Composable
    override fun present(): State {

        val users by produceState<ListLoadingState>(initialValue = ListLoadingState.Loading) {
            value = userRepository.getUsers(screen.userFids).let { profiles ->
                ListLoadingState.Loaded(profiles)
            }
        }

        return State(
            screen.title,
            users
        ) { event ->
            when (event) {
                Event.NavigateBack -> navigator.pop()
                is Event.NavigateToProfile -> navigator.goTo(ProfileScreen(event.fid))
            }
        }
    }

    @CircuitInject(UserListScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator, screen: UserListScreen): UserListPresenter
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(UserListScreen::class, AppScope::class)
@Composable
fun UserListView(userListState: State, modifier: Modifier = Modifier) {
    val eventSink = userListState.eventSink
    val userCount = userListState.loadingState.items.count()
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(stringResource(userListState.title))
            }, navigationIcon = {
                IconButton(onClick = {
                    // clicked back
                    eventSink(Event.NavigateBack)
                }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                }
            })
        },) { paddingValues ->
        // the list of users
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(count = userCount) { idx ->
                val profile = userListState.loadingState.items[idx]
                ProfileListItem(profile = profile, modifier = Modifier.clickable {
                    eventSink(Event.NavigateToProfile(profile.fid))
                })
            }
        }
    }
}