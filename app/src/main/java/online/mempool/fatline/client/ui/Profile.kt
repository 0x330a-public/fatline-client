@file:OptIn(ExperimentalMaterial3Api::class)

package online.mempool.fatline.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import online.mempool.fatline.client.R
import online.mempool.fatline.client.screens.FollowLoadingState
import online.mempool.fatline.client.screens.ProfileLoadingState
import online.mempool.fatline.client.screens.ProfileScreen.Event
import online.mempool.fatline.client.ui.theme.FatlineTheme
import online.mempool.fatline.data.Profile

@Composable
fun ProfileAvatar(pfp: String?) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .padding(16.dp)
            .size(48.dp, 48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceDim)
            .border(1.dp, MaterialTheme.colorScheme.surface, shape = CircleShape)
    ) {
        val requestBuilder = ImageRequest.Builder(context)
        if (!pfp.isNullOrEmpty()) {
            AsyncImage(
                model = requestBuilder
                    .data(pfp)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                imageLoader = context.imageLoader,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun ProfileListItem(profile: Profile, modifier: Modifier = Modifier) {
    val displayName = profile.display_name ?: stringResource(R.string.default_username)
    val pfp = profile.profile_picture

    Row(modifier) {
        ProfileAvatar(pfp = pfp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            horizontalArrangement = Arrangement.Start,
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(displayName)
                Text(profile.displayTag())
            }
        }
    }

}

@Composable
fun ProfileBanner(profileState: ProfileLoadingState,
                  followingState: FollowLoadingState,
                  followsState: FollowLoadingState,
                  eventSink: (Event)->Unit,
                  modifier: Modifier = Modifier) {

    val profile = (profileState as? ProfileLoadingState.Loaded)?.userProfile

    val displayName = profile?.display_name ?: stringResource(R.string.default_username)


    val pfp = profile?.profile_picture

    Box(modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
        Column {
            Row {
                ProfileAvatar(pfp = pfp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        when(profileState) {
                            is ProfileLoadingState.Loaded -> {
                                Text(displayName)
                                Text(profileState.userProfile.displayTag())
                            }
                            is ProfileLoadingState.LoadError -> {

                            }
                            ProfileLoadingState.Loading -> {
                                // give it a spinner or something idk
                                Text(stringResource(R.string.loading_placeholder))
                            }
                        }
                    }
                    if ((profileState as? ProfileLoadingState.Loaded)?.isUs == true) {
                        ElevatedButton(
                            modifier = Modifier.padding(8.dp),
                            onClick = { eventSink(Event.EditProfile) }
                        ) {
                            Text(stringResource(id = R.string.edit_profile))
                        }
                    }
                }
            }
            // Follower info
            Row(
                modifier = Modifier.padding(16.dp, 8.dp),
            ) {
                val childModifier = Modifier.padding(8.dp)
                ElevatedButton(
                    modifier = childModifier,
                    onClick = {
                        if (followingState is FollowLoadingState.Loaded) {
                            eventSink(Event.OpenFollowing(followingState.profiles.map(Profile::fid)))
                        }
                    }) {
                    if (followingState is FollowLoadingState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp
                        )
                        Text(stringResource(id = R.string.following_placeholder))
                    } else if (followingState is FollowLoadingState.Loaded) {
                        Text(stringResource(id = R.string.following_number,
                            followingState.profiles.size))
                    }
                }
                ElevatedButton(
                    modifier = childModifier,
                    onClick = {
                        if (followsState is FollowLoadingState.Loaded) {
                            eventSink(Event.OpenFollowers(followsState.profiles.map(Profile::fid)))
                        }
                    }) {
                    if (followsState is FollowLoadingState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp
                        )
                        Text(stringResource(id = R.string.followers_placeholder))
                    } else if (followsState is FollowLoadingState.Loaded){
                        Text(
                            stringResource(
                                id = R.string.followers_number,
                                followsState.profiles.size
                            )
                        )
                    }
                }
            }
        }
        if (profileState == ProfileLoadingState.Loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            )
        }
    }


}

@Preview(showSystemUi = true)
@Composable
fun PreviewProfileList() {
    val users = listOf(
        Profile(
            fid=1,
            username="test-user1",
            display_name = "Some User",
            profile_picture = null,
            bio = null,
            url = null,
        ),
        Profile(
            fid=1,
            username="user2",
            display_name = "A New User",
            profile_picture = null,
            bio = null,
            url = null,
        ),
        Profile(
            fid=1,
            username="reallycoolguy123",
            display_name = "third_user",
            profile_picture = null,
            bio = null,
            url = null,
        )
    )

    FatlineTheme {
        Box(Modifier.fillMaxSize()) {
            Column {
                users.map {
                    ProfileListItem(profile = it)
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewProfile() {
    FatlineTheme {
        Box(Modifier.fillMaxSize()) {
            ProfileBanner(
                ProfileLoadingState.Loaded(
                    Profile(
                        fid=1,
                        username="test-user",
                        display_name = "Test User",
                        profile_picture = null,
                        bio = null,
                        url = null,
                    ),
                    isUs = true
                ),
                followingState = FollowLoadingState.Loading,
                followsState = FollowLoadingState.Loading,
                eventSink = {}
            )
        }
    }
}