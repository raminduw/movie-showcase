package dev.ramindu.cinenote.feature.favorites.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.ramindu.cinenote.core.designsystem.component.LocalNavAnimatedVisibilityScope
import dev.ramindu.cinenote.feature.favorites.FavoritesRoute
import kotlinx.serialization.Serializable

@Serializable
data object FavoritesDestination

fun NavGraphBuilder.favoritesScreen(
    onMovieClick: (movieId: Int) -> Unit,
    onBrowseMovies: () -> Unit,
) {
    composable<FavoritesDestination> {
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            FavoritesRoute(onMovieClick = onMovieClick, onBrowseMovies = onBrowseMovies)
        }
    }
}
