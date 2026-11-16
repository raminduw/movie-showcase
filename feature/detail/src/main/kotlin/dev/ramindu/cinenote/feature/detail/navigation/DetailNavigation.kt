package dev.ramindu.cinenote.feature.detail.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.ramindu.cinenote.core.designsystem.component.LocalNavAnimatedVisibilityScope
import dev.ramindu.cinenote.feature.detail.DetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailDestination(val movieId: Int) {
    companion object {
        /** Must match the property name above – it's the SavedStateHandle key. */
        const val ARG_MOVIE_ID = "movieId"
    }
}

fun NavController.navigateToMovieDetail(movieId: Int) = navigate(MovieDetailDestination(movieId))

fun NavGraphBuilder.movieDetailScreen(
    onBack: () -> Unit,
    onMovieClick: (movieId: Int) -> Unit,
) {
    composable<MovieDetailDestination> {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            DetailRoute(onBack = onBack, onMovieClick = onMovieClick)
        }
    }
}
