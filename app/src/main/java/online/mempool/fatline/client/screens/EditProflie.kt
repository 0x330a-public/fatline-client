package online.mempool.fatline.client.screens

import MessageOuterClass.UserDataBody
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import online.mempool.fatline.client.R
import online.mempool.fatline.client.ui.theme.FatlineTheme
import online.mempool.fatline.data.Profile
import online.mempool.fatline.data.Profile.Companion.UpdatedField.BIO
import online.mempool.fatline.data.Profile.Companion.UpdatedField.DISPLAY_NAME
import online.mempool.fatline.data.Profile.Companion.UpdatedField.URL
import online.mempool.fatline.data.Profile.Companion.profileUpdateBody
import online.mempool.fatline.data.di.AppScope

@Parcelize
data object EditProfileScreen: Screen {
    sealed interface Event: CircuitUiEvent {
        data class UpdateFields(val updates: List<UserDataBody>) : Event
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
                is EditProfileScreen.Event.UpdateFields -> {
                    userRepository.postUpdates(event.updates)
                }
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

    val profile = (state.profileState as? ProfileLoadingState.Loaded)?.userProfile
    val eventSink = state.eventSink

    val fid = rememberSaveable(profile) {
        profile?.fid ?: 0
    }

    val username = rememberSaveable(profile) {
        profile?.username.orEmpty()
    }

    var displayName by rememberSaveable(profile) {
        mutableStateOf(profile?.display_name)
    }

    var bio by rememberSaveable(profile) {
        mutableStateOf(profile?.bio)
    }

    var url by rememberSaveable(profile) {
        mutableStateOf(profile?.url)
    }

    var profilePic by rememberSaveable(profile) {
        mutableStateOf(profile?.profile_picture)
    }

    val hasDifferences = profile != null &&
            (profile.display_name != displayName ||
                    profile.bio != bio || profile.url != url)


    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.edit_profile)) }, navigationIcon = {
            IconButton(onClick = { state.eventSink(EditProfileScreen.Event.NavigateBack) }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
        })
    }, modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val childModifiers = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            // username, currently can't be used until implement FName
            OutlinedTextField(
                value = username,
                onValueChange = {},
                label = { Text(stringResource(R.string.username_field)) },
                enabled = false,
                modifier = childModifiers
            )

            // display name
            OutlinedTextField(
                value = displayName.orEmpty(),
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.displayname_field)) },
                modifier = childModifiers,
                enabled = profile != null
            )

            // bio
            OutlinedTextField(
                value = bio.orEmpty(),
                onValueChange = { bio = it },
                label = { Text(stringResource(R.string.bio_field)) },
                modifier = childModifiers,
                enabled = profile != null
            )

            // url
            OutlinedTextField(
                value = url.orEmpty(),
                onValueChange = { url = it },
                label = { Text(stringResource(R.string.url_field)) },
                modifier = childModifiers,
                enabled = profile != null
            )

            // confirm button
            FilledTonalButton(onClick = {
                val updates = buildList {
                    val d = displayName.orEmpty()
                    val b = bio.orEmpty()
                    val u = url.orEmpty()
                    profile?.let { p ->
                        if (d.isNotEmpty() && d != p.display_name) {
                            add(profileUpdateBody(DISPLAY_NAME, d))
                        }
                        if (b.isNotEmpty() && b != p.bio) {
                            add(profileUpdateBody(BIO, b))
                        }
                        if (u.isNotEmpty() && u != p.url) {
                            add(profileUpdateBody(URL, u))
                        }
                    }
                }
                eventSink(EditProfileScreen.Event.UpdateFields(updates))
            },
                modifier = childModifiers
                    .padding(horizontal = 64.dp)
                    .padding(top = 24.dp),
                enabled = hasDifferences
            ) {
                // contents
                Text(stringResource(R.string.save_profile))
            }

            // fid?
            Text(
                stringResource(R.string.fid_field, fid),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun PreviewEditProfile() {

    val state = EditProfileScreen.State(
        ProfileLoadingState.Loaded(
            userProfile = Profile(
                fid = 1,
                username = "testuser",
                display_name = "Test User",
                profile_picture = null,
                bio = null,
                url = null
            ),
            isUs = false,
        )
    ) {

    }
    FatlineTheme {
        EditProfile(state)
    }
}