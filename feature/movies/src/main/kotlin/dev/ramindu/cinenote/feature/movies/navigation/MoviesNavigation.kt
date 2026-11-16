package dev.ramindu.cinenote.feature.movies.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.ramindu.cinenote.core.designsystem.component.LocalNavAnimatedVisibilityScope
import dev.ramindu.cinenote.feature.movies.MoviesRoute
import kotlinx.serialization.Serializable

/** Type-safe route. The feature owns it; :app only references it. */
@Serializable
data object MoviesDestination

fun NavGraphBuilder.moviesScreen(onMovieClick: (movieId: Int) -> Unit) {
    composable<MoviesDestination> {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            MoviesRoute(onMovieClick = onMovieClick)
        }
    }
}
