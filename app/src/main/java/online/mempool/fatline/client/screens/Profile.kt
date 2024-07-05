@file:OptIn(ExperimentalMaterial3Api::class)

package online.mempool.fatline.client.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.parcelize.Parcelize
import online.mempool.fatline.api.UserRepository
import online.mempool.fatline.client.R
import online.mempool.fatline.client.ui.ProfileBanner
import online.mempool.fatline.data.di.AppScope

@Parcelize
data class ProfileScreen(val fid: Long?): Screen {
    sealed interface Event: CircuitUiEvent {
        data class NavigateToProfile(val fid: Long? = null): Event
        data object EditProfile: Event
    }
    data class State(
        val isRoot: Boolean,
        val profileState: ProfileLoadingState,
        val followingState: FollowLoadingState,
        val followsState: FollowLoadingState,
        val eventSink: (Event) -> Unit
    ): CircuitUiState
}

class ProfilePresenter @AssistedInject constructor(@Assisted private val navigator: Navigator,
                                                   @Assisted private val screen: ProfileScreen,
                                                   private val userRepository: UserRepository
): Presenter<ProfileScreen.State> {

    @Composable
    override fun present(): ProfileScreen.State {

        val refreshes = remember {
            Channel<Unit>(Channel.CONFLATED)
        }

        val userFlow = remember {
            userRepository.user(screen.fid).map { profile ->
                ProfileLoadingState.Loaded(profile, profile.fid == userRepository.currentFid())
            }
        }

        val followsFlow = remember {
            userRepository.follows(screen.fid, refreshes.receiveAsFlow()).map { follows ->
                FollowLoadingState.Loaded(follows)
            }
        }

        val followingFlow = remember {
            userRepository.following(screen.fid, refreshes.receiveAsFlow()).map {
                FollowLoadingState.Loaded(it)
            }
        }

        val userState by userFlow.collectAsRetainedState(initial = ProfileLoadingState.Loading)
        val followingState by followingFlow.collectAsRetainedState(initial = FollowLoadingState.Loading)
        val followsState by followsFlow.collectAsRetainedState(initial = FollowLoadingState.Loading)

        return ProfileScreen.State(
            isRoot = navigator.peekBackStack().first() == screen,
            profileState = userState,
            followingState = followingState,
            followsState = followsState
        ) { event ->
            when (event) {
                is ProfileScreen.Event.NavigateToProfile -> navigator.goTo(ProfileScreen(event.fid))
                is ProfileScreen.Event.EditProfile -> navigator.goTo(EditProfileScreen)
            }
        }

    }

    @CircuitInject(ProfileScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator, screen: ProfileScreen): ProfilePresenter
    }
}

@CircuitInject(ProfileScreen::class, AppScope::class)
@Composable
fun Profile(state: ProfileScreen.State, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    val showBack = !state.isRoot

    val username = (state.profileState as? ProfileLoadingState.Loaded)?.userProfile?.username
        ?: stringResource(id = R.string.default_username)

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(username)
            }, navigationIcon = {
                if (showBack) {
                    IconButton(onClick = {
                        // clicked back
                    }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                }
            })
        },
        content = { padding ->
            Column(
                modifier
                    .padding(padding)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)) {
                ProfileBanner(
                    profileState = state.profileState,
                    followsState = state.followsState,
                    followingState = state.followingState,
                    eventSink = state.eventSink
                )
            }
        }
    ) }