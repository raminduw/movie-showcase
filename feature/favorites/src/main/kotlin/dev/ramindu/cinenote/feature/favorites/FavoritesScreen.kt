package dev.ramindu.cinenote.feature.favorites

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ramindu.cinenote.core.designsystem.component.EmptyState
import dev.ramindu.cinenote.core.designsystem.component.FavoriteButton
import dev.ramindu.cinenote.core.designsystem.component.MoviePoster
import dev.ramindu.cinenote.core.designsystem.component.NoteEditorLabels
import dev.ramindu.cinenote.core.designsystem.component.NoteEditorSheet
import dev.ramindu.cinenote.core.designsystem.component.RatingBadge
import dev.ramindu.cinenote.core.designsystem.component.SearchField
import dev.ramindu.cinenote.core.designsystem.component.sharedPoster
import dev.ramindu.cinenote.core.designsystem.preview.PreviewTheme
import dev.ramindu.cinenote.core.designsystem.preview.ScreenPreviews
import dev.ramindu.cinenote.core.designsystem.preview.ThemePreviews
import dev.ramindu.cinenote.core.domain.usecase.UpdateFavoriteNoteUseCase
import dev.ramindu.cinenote.core.ui.mvi.ObserveEffects
import dev.ramindu.cinenote.core.ui.R as CoreUiR

@Composable
fun FavoritesRoute(
    onMovieClick: (Int) -> Unit,
    onBrowseMovies: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            is FavoritesEffect.NavigateToDetail -> onMovieClick(effect.movieId)
            FavoritesEffect.NavigateToMovies -> onBrowseMovies()
            is FavoritesEffect.ShowUndo -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.favorites_removed, effect.movieTitle),
                    actionLabel = context.getString(R.string.favorites_undo),
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.onAction(FavoritesAction.UndoRemove)
            }
            is FavoritesEffect.ShowMessage -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(context.getString(effect.messageRes, *effect.args.toTypedArray()))
            }
        }
    }

    FavoritesScreen(state = state, snackbarHostState = snackbarHostState, onAction = viewModel::onAction)
}

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (FavoritesAction) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.favorites_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    pluralStringResource(R.plurals.favorites_count, state.items.size, state.items.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                if (state.items.isNotEmpty() || state.isSearching) {
                    SearchField(
                        query = state.query,
                        onQueryChange = { onAction(FavoritesAction.QueryChanged(it)) },
                        placeholder = stringResource(R.string.favorites_search_hint),
                        clearContentDescription = stringResource(CoreUiR.string.cd_clear_search),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            when {
                state.isLoading -> Unit
                state.items.isEmpty() && state.isSearching -> EmptyState(
                    icon = Icons.Filled.Search,
                    title = stringResource(R.string.favorites_no_match_title),
                    message = stringResource(R.string.favorites_no_match_message, state.query.trim()),
                    modifier = Modifier.padding(top = 32.dp),
                )
                state.items.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Favorite,
                    title = stringResource(R.string.favorites_empty_title),
                    message = stringResource(R.string.favorites_empty_message),
                    actionLabel = stringResource(R.string.favorites_browse),
                    onAction = { onAction(FavoritesAction.BrowseMovies) },
                    modifier = Modifier.padding(top = 48.dp),
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.items, key = { it.id }) { favorite ->
                        SwipeToRemove(onRemove = { onAction(FavoritesAction.Remove(favorite.id)) }) {
                            FavoriteCard(favorite = favorite, onAction = onAction)
                        }
                    }
                }
            }
        }
    }

    state.editingNoteFor?.let { favorite ->
        NoteEditorSheet(
            title = favorite.title,
            initialNote = favorite.note.orEmpty(),
            maxLength = UpdateFavoriteNoteUseCase.MAX_NOTE_LENGTH,
            labels = NoteEditorLabels(
                heading = stringResource(CoreUiR.string.note_heading),
                placeholder = stringResource(CoreUiR.string.note_placeholder),
                save = stringResource(CoreUiR.string.note_save),
                cancel = stringResource(CoreUiR.string.note_cancel),
            ),
            onSave = { onAction(FavoritesAction.SaveNote(it)) },
            onDismiss = { onAction(FavoritesAction.DismissNoteEditor) },
        )
    }
}

@Composable
private fun SwipeToRemove(onRemove: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onRemove()
            value == SwipeToDismissBoxValue.EndToStart
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
    ) { content() }
}

// ---------- Previews (light + dark) ----------

private val previewFavorites = listOf(
    FavoriteUi(1, "Arrival", "preview", "2016", "7.6", "Language as a weapon. Watch again with subtitles on."),
    FavoriteUi(2, "The Shawshank Redemption", "preview", "1994", "8.7", null),
    FavoriteUi(3, "Dune: Part Two", null, "2024", "8.2", "IMAX only!"),
)

@ScreenPreviews
@Composable
private fun FavoritesScreenPreview() = PreviewTheme {
    FavoritesScreen(
        state = FavoritesUiState(items = previewFavorites, isLoading = false),
        snackbarHostState = remember { SnackbarHostState() },
        onAction = {},
    )
}

@ThemePreviews
@Composable
private fun FavoritesEmptyPreview() = PreviewTheme {
    FavoritesScreen(
        state = FavoritesUiState(isLoading = false),
        snackbarHostState = remember { SnackbarHostState() },
        onAction = {},
    )
}

@ThemePreviews
@Composable
private fun FavoriteCardPreview() = PreviewTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FavoriteCard(previewFavorites[0], onAction = {})
        FavoriteCard(previewFavorites[1], onAction = {})
    }
}

@Composable
private fun FavoriteCard(favorite: FavoriteUi, onAction: (FavoritesAction) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onAction(FavoritesAction.OpenMovie(favorite.id)) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(10.dp)) {
            Box {
                MoviePoster(
                    url = favorite.posterUrl,
                    contentDescription = favorite.title,
                    modifier = Modifier.width(72.dp).height(108.dp).sharedPoster(favorite.id),
                )
                RatingBadge(favorite.ratingLabel, Modifier.align(Alignment.BottomStart).padding(4.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp, top = 2.dp)) {
                Text(favorite.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(favorite.yearLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAction(FavoritesAction.EditNote(favorite.id)) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(
                            if (favorite.note == null) R.string.favorites_add_note else R.string.favorites_edit_note,
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 6.dp, top = 2.dp).height(14.dp).width(14.dp),
                    )
                    Text(
                        text = favorite.note ?: stringResource(R.string.favorites_add_note),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = if (favorite.note == null) FontStyle.Normal else FontStyle.Italic,
                        color = if (favorite.note == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FavoriteButton(
                isFavorite = true,
                onClick = { onAction(FavoritesAction.Remove(favorite.id)) },
                contentDescription = stringResource(CoreUiR.string.cd_remove_favorite),
            )
        }
    }
}
