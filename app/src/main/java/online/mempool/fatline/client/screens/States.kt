package online.mempool.fatline.client.screens

import online.mempool.fatline.data.Profile

// lazy loading profile state
sealed interface ProfileLoadingState {
    data object Loading: ProfileLoadingState
    data class Loaded(
        val userProfile: Profile,
        val isUs: Boolean
    ): ProfileLoadingState
    data class LoadError(
        val reason: Throwable
    ): ProfileLoadingState
}
// lazy loading follow state
sealed interface FollowLoadingState {
    data object Loading: FollowLoadingState
    // loaded also counts as error
    data class Loaded(
        val profiles: List<Profile> = emptyList(),
    ): FollowLoadingState
}