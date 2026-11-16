package dev.ramindu.cinenote.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.ramindu.cinenote.R
import dev.ramindu.cinenote.core.designsystem.component.LocalSharedTransitionScope
import dev.ramindu.cinenote.feature.detail.navigation.movieDetailScreen
import dev.ramindu.cinenote.feature.detail.navigation.navigateToMovieDetail
import dev.ramindu.cinenote.feature.favorites.navigation.FavoritesDestination
import dev.ramindu.cinenote.feature.favorites.navigation.favoritesScreen
import dev.ramindu.cinenote.feature.movies.navigation.MoviesDestination
import dev.ramindu.cinenote.feature.movies.navigation.moviesScreen
import kotlin.reflect.KClass

/** Top-level tabs. Features don't know each other – :app connects them here. */
private enum class TopLevelTab(val route: Any, val routeClass: KClass<*>, val icon: ImageVector, val labelRes: Int) {
    MOVIES(MoviesDestination, MoviesDestination::class, Icons.Filled.Home, R.string.tab_movies),
    FAVORITES(FavoritesDestination, FavoritesDestination::class, Icons.Filled.Favorite, R.string.tab_favorites),
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CineNoteApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = TopLevelTab.entries.any { currentDestination.isOn(it) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    TopLevelTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentDestination.isOn(tab),
                            onClick = { navController.navigateToTab(tab) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        SharedTransitionLayout(Modifier.fillMaxSize().padding(padding)) {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(navController = navController, startDestination = MoviesDestination) {
                    moviesScreen(onMovieClick = navController::navigateToMovieDetail)
                    favoritesScreen(
                        onMovieClick = navController::navigateToMovieDetail,
                        onBrowseMovies = { navController.navigateToTab(TopLevelTab.MOVIES) },
                    )
                    movieDetailScreen(
                        onBack = { navController.popBackStack() },
                        onMovieClick = navController::navigateToMovieDetail,
                    )
                }
            }
        }
    }
}

private fun NavDestination?.isOn(tab: TopLevelTab): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true

/** Standard tab behaviour: single copy per tab, state saved/restored when switching. */
private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
