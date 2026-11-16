package dev.ramindu.cinenote.feature.movies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import dev.ramindu.cinenote.core.designsystem.component.EmptyState
import dev.ramindu.cinenote.core.designsystem.component.ErrorState
import dev.ramindu.cinenote.core.designsystem.component.FavoriteButton
import dev.ramindu.cinenote.core.designsystem.component.MoviePoster
import dev.ramindu.cinenote.core.designsystem.component.RatingBadge
import dev.ramindu.cinenote.core.designsystem.component.SearchField
import dev.ramindu.cinenote.core.designsystem.component.sharedPoster
import dev.ramindu.cinenote.core.designsystem.component.shimmer
import dev.ramindu.cinenote.core.designsystem.preview.PreviewTheme
import dev.ramindu.cinenote.core.designsystem.preview.ScreenPreviews
import dev.ramindu.cinenote.core.designsystem.preview.ThemePreviews
import kotlinx.coroutines.flow.flowOf
import dev.ramindu.cinenote.core.domain.model.MovieCategory
import dev.ramindu.cinenote.core.ui.copyMovieLink
import dev.ramindu.cinenote.core.ui.messageRes
import dev.ramindu.cinenote.core.ui.ShareMenuButton
import dev.ramindu.cinenote.core.ui.openShareSheet
import dev.ramindu.cinenote.core.ui.mvi.ObserveEffects
import dev.ramindu.cinenote.core.ui.toAppError
import dev.ramindu.cinenote.core.ui.R as CoreUiR

/** Stateful entry point: wires the ViewModel. */
@Composable
fun MoviesRoute(
    onMovieClick: (Int) -> Unit,
    viewModel: MoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            is MoviesEffect.NavigateToDetail -> onMovieClick(effect.movieId)
            is MoviesEffect.ShowMessage -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(context.getString(effect.messageRes, effect.movieTitle))
            }
            is MoviesEffect.OpenShareSheet -> context.openShareSheet(effect.share)
            is MoviesEffect.CopyLink -> if (context.copyMovieLink(effect.share)) {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(context.getString(CoreUiR.string.share_link_copied))
            }
        }
    }

    MoviesScreen(state = state, movies = movies, snackbarHostState = snackbarHostState, onAction = viewModel::onAction)
}

/** Stateless screen: easy to preview and to test. */
@Composable
fun MoviesScreen(
    state: MoviesUiState,
    movies: LazyPagingItems<MovieUi>,
    snackbarHostState: SnackbarHostState,
    onAction: (MoviesAction) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // The bottom bar (in :app) already handles the bottom inset.
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Header(state = state, onAction = onAction)
            MovieList(state = state, movies = movies, onAction = onAction)
        }
    }
}

@Composable
private fun Header(state: MoviesUiState, onAction: (MoviesAction) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.movies_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.movies_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        SearchField(
            query = state.query,
            onQueryChange = { onAction(MoviesAction.QueryChanged(it)) },
            placeholder = stringResource(R.string.movies_search_hint),
            clearContentDescription = stringResource(CoreUiR.string.cd_clear_search),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    AnimatedVisibility(visible = !state.isSearching) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(MovieCategory.entries) { category ->
                FilterChip(
                    selected = category == state.category,
                    onClick = { onAction(MoviesAction.CategorySelected(category)) },
                    label = { Text(stringResource(category.labelRes())) },
                )
            }
        }
    }
    if (state.isSearching) {
        Text(
            text = stringResource(R.string.movies_search_results, state.query.trim()),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun MovieList(
    state: MoviesUiState,
    movies: LazyPagingItems<MovieUi>,
    onAction: (MoviesAction) -> Unit,
) {
    val refresh = movies.loadState.refresh
    when {
        refresh is LoadState.Loading && movies.itemCount == 0 -> LoadingList()
        refresh is LoadState.Error && movies.itemCount == 0 -> ErrorState(
            title = stringResource(CoreUiR.string.error_title),
            message = stringResource(refresh.error.toAppError().messageRes()),
            retryLabel = stringResource(CoreUiR.string.retry),
            onRetry = movies::retry,
            modifier = Modifier.padding(top = 48.dp),
        )
        refresh is LoadState.NotLoading && movies.itemCount == 0 -> EmptyState(
            icon = Icons.Filled.Search,
            title = stringResource(if (state.isSearching) R.string.movies_empty_search_title else R.string.movies_empty_title),
            message = if (state.isSearching) {
                stringResource(R.string.movies_empty_search_message, state.query.trim())
            } else {
                stringResource(R.string.movies_empty_message)
            },
            modifier = Modifier.padding(top = 48.dp),
        )
        else -> LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(count = movies.itemCount, key = movies.itemKey { it.id }) { index ->
                val movie = movies[index] ?: return@items
                MovieCard(
                    movie = movie,
                    isFavorite = movie.id in state.favoriteIds,
                    onClick = { onAction(MoviesAction.MovieClicked(movie.id)) },
                    onToggleFavorite = { onAction(MoviesAction.ToggleFavorite(movie)) },
                    onShare = { onAction(MoviesAction.ShareMovie(movie)) },
                    onCopyLink = { onAction(MoviesAction.CopyMovieLink(movie)) },
                )
            }
            when (val append = movies.loadState.append) {
                is LoadState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(28.dp))
                    }
                }
                is LoadState.Error -> item {
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(append.error.toAppError().messageRes()),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = movies::retry) { Text(stringResource(CoreUiR.string.retry)) }
                    }
                }
                is LoadState.NotLoading -> Unit
            }
        }
    }
}

@Composable
private fun MovieCard(
    movie: MovieUi,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(10.dp)) {
            Box {
                MoviePoster(
                    url = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.width(92.dp).height(138.dp).sharedPoster(movie.id),
                )
                RatingBadge(movie.ratingLabel, Modifier.align(Alignment.BottomStart).padding(6.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp, top = 4.dp)) {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    movie.yearLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    movie.overview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FavoriteButton(
                    isFavorite = isFavorite,
                    onClick = onToggleFavorite,
                    contentDescription = stringResource(
                        if (isFavorite) CoreUiR.string.cd_remove_favorite else CoreUiR.string.cd_add_favorite,
                    ),
                )
                ShareMenuButton(
                    movieTitle = movie.title,
                    onShare = onShare,
                    onCopyLink = onCopyLink,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        }
    }
}

/** Skeleton cards that mirror [MovieCard] while the first page loads. */
@Composable
private fun LoadingList() {
    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(5) {
            Row(Modifier.fillMaxWidth().padding(10.dp)) {
                Box(Modifier.width(92.dp).height(138.dp).shimmerShape())
                Column(Modifier.padding(start = 12.dp, top = 4.dp)) {
                    Box(Modifier.fillMaxWidth(0.7f).height(18.dp).shimmerShape())
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(48.dp).height(14.dp).shimmerShape())
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(12.dp).shimmerShape())
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth(0.85f).height(12.dp).shimmerShape())
                }
            }
        }
    }
}

@Composable
private fun Modifier.shimmerShape(): Modifier = this.clip(RoundedCornerShape(8.dp)).shimmer()

// ---------- Previews (light + dark) ----------

private val previewMovies = listOf(
    MovieUi(1, "Dune: Part Two", "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.", "preview", 2024, "2024", 8.2, "8.2"),
    MovieUi(2, "Inside Out 2", "Teenager Riley's mind headquarters is undergoing a sudden demolition to make room for new Emotions.", "preview", 2024, "2024", 7.6, "7.6"),
    MovieUi(3, "Untitled Sci-Fi Project", "", null, null, "TBA", 0.0, "–"),
)

/** Static paging data with "nothing more to load" states, so LazyPagingItems renders in previews. */
@Composable
private fun previewPagingItems(movies: List<MovieUi>): LazyPagingItems<MovieUi> {
    val notLoading = LoadState.NotLoading(endOfPaginationReached = true)
    return remember(movies) {
        flowOf(PagingData.from(movies, sourceLoadStates = LoadStates(notLoading, notLoading, notLoading)))
    }.collectAsLazyPagingItems()
}

@ThemePreviews
@Composable
private fun MovieCardPreview() = PreviewTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MovieCard(previewMovies[0], isFavorite = true, onClick = {}, onToggleFavorite = {}, onShare = {}, onCopyLink = {})
        MovieCard(previewMovies[2], isFavorite = false, onClick = {}, onToggleFavorite = {}, onShare = {}, onCopyLink = {})
    }
}

@ThemePreviews
@Composable
private fun LoadingListPreview() = PreviewTheme { LoadingList() }

@ScreenPreviews
@Composable
private fun MoviesScreenPreview() = PreviewTheme {
    MoviesScreen(
        state = MoviesUiState(favoriteIds = setOf(1)),
        movies = previewPagingItems(previewMovies),
        snackbarHostState = remember { SnackbarHostState() },
        onAction = {},
    )
}

@ScreenPreviews
@Composable
private fun MoviesScreenNoResultsPreview() = PreviewTheme {
    MoviesScreen(
        state = MoviesUiState(query = "asdfgh"),
        movies = previewPagingItems(emptyList()),
        snackbarHostState = remember { SnackbarHostState() },
        onAction = {},
    )
}

private fun MovieCategory.labelRes(): Int = when (this) {
    MovieCategory.POPULAR -> R.string.movies_category_popular
    MovieCategory.TRENDING -> R.string.movies_category_trending
    MovieCategory.TOP_RATED -> R.string.movies_category_top_rated
    MovieCategory.NOW_PLAYING -> R.string.movies_category_now_playing
    MovieCategory.UPCOMING -> R.string.movies_category_upcoming
}
