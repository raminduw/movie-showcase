package dev.ramindu.cinenote.feature.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ramindu.cinenote.core.designsystem.component.ErrorState
import dev.ramindu.cinenote.core.designsystem.component.MoviePoster
import dev.ramindu.cinenote.core.designsystem.component.NoteEditorLabels
import dev.ramindu.cinenote.core.designsystem.component.NoteEditorSheet
import dev.ramindu.cinenote.core.designsystem.component.RatingBadge
import dev.ramindu.cinenote.core.designsystem.component.sharedPoster
import dev.ramindu.cinenote.core.designsystem.component.shimmer
import dev.ramindu.cinenote.core.designsystem.preview.PreviewTheme
import dev.ramindu.cinenote.core.designsystem.preview.ScreenPreviews
import dev.ramindu.cinenote.core.designsystem.preview.ThemePreviews
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.usecase.UpdateFavoriteNoteUseCase
import dev.ramindu.cinenote.core.ui.ShareMenuButton
import dev.ramindu.cinenote.core.ui.copyMovieLink
import dev.ramindu.cinenote.core.ui.messageRes
import dev.ramindu.cinenote.core.ui.openShareSheet
import dev.ramindu.cinenote.core.ui.mvi.ObserveEffects
import dev.ramindu.cinenote.core.ui.R as CoreUiR

@Composable
fun DetailRoute(
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            DetailEffect.NavigateBack -> onBack()
            is DetailEffect.NavigateToMovie -> onMovieClick(effect.movieId)
            is DetailEffect.OpenUrl -> runCatching { uriHandler.openUri(effect.url) }
            is DetailEffect.ShowMessage -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(context.getString(effect.messageRes, *effect.args.toTypedArray()))
            }
            is DetailEffect.OpenShareSheet -> context.openShareSheet(effect.share)
            is DetailEffect.CopyLink -> if (context.copyMovieLink(effect.share)) {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(context.getString(CoreUiR.string.share_link_copied))
            }
        }
    }

    DetailScreen(state = state, snackbarHostState = snackbarHostState, onAction = viewModel::onAction)
}

@Composable
fun DetailScreen(
    state: DetailUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (DetailAction) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val detail = state.detail
            when {
                detail != null -> DetailContent(detail = detail, state = state, onAction = onAction)
                state.error != null -> ErrorState(
                    title = stringResource(CoreUiR.string.error_title),
                    message = stringResource(state.error.messageRes()),
                    retryLabel = stringResource(CoreUiR.string.retry),
                    onRetry = { onAction(DetailAction.Retry) },
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> DetailLoading()
            }
            HeaderButtons(
                movieTitle = detail?.title,
                onBack = { onAction(DetailAction.BackClicked) },
                onShare = { onAction(DetailAction.Share) },
                onCopyLink = { onAction(DetailAction.CopyLink) },
            )
        }
    }

    val detail = state.detail
    if (state.isNoteEditorOpen && detail != null) {
        NoteEditorSheet(
            title = detail.title,
            initialNote = state.note.orEmpty(),
            maxLength = UpdateFavoriteNoteUseCase.MAX_NOTE_LENGTH,
            labels = noteLabels(),
            onSave = { onAction(DetailAction.SaveNote(it)) },
            onDismiss = { onAction(DetailAction.DismissNoteEditor) },
        )
    }
}

@Composable
private fun DetailContent(detail: MovieDetailUi, state: DetailUiState, onAction: (DetailAction) -> Unit) {
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Edge-to-edge: content scrolls behind the system bar but the last row stays reachable.
        contentPadding = PaddingValues(bottom = 32.dp + navBarBottom),
    ) {
        item { Hero(detail) }
        item { TitleBlock(detail) }
        item {
            ActionButtons(
                isFavorite = state.isFavorite,
                hasTrailer = detail.trailerUrl != null,
                onToggleFavorite = { onAction(DetailAction.ToggleFavorite) },
                onPlayTrailer = { onAction(DetailAction.PlayTrailer) },
            )
        }
        item {
            AnimatedVisibility(visible = state.isFavorite) {
                NoteCard(note = state.note, onEdit = { onAction(DetailAction.EditNote) })
            }
        }
        item {
            Section(stringResource(R.string.detail_overview)) {
                Text(
                    detail.overview.ifBlank { stringResource(R.string.detail_no_overview) },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        if (detail.cast.isNotEmpty()) {
            item {
                Section(stringResource(R.string.detail_cast)) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(detail.cast, key = { it.id }) { CastItem(it) }
                    }
                }
            }
        }
        if (detail.similar.isNotEmpty()) {
            item {
                Section(stringResource(R.string.detail_similar)) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(detail.similar, key = { it.id }) { movie ->
                            SimilarItem(movie, onClick = { onAction(DetailAction.SimilarMovieClicked(movie.id)) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(detail: MovieDetailUi) {
    val background = MaterialTheme.colorScheme.background
    Box(Modifier.fillMaxWidth().height(340.dp)) {
        MoviePoster(
            url = detail.backdropUrl,
            contentDescription = null,
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth().height(260.dp),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to background)),
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            MoviePoster(
                url = detail.posterUrl,
                contentDescription = detail.title,
                modifier = Modifier.width(120.dp).height(180.dp).sharedPoster(detail.id),
            )
            Column(Modifier.padding(start = 16.dp, bottom = 8.dp)) {
                RatingBadge(detail.ratingLabel)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.detail_votes, detail.voteCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TitleBlock(detail: MovieDetailUi) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(detail.title, style = MaterialTheme.typography.headlineSmall)
        val meta = listOfNotNull(detail.yearLabel, detail.runtimeLabel).joinToString("  •  ")
        Text(
            meta,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
        )
        detail.tagline?.let {
            Text(
                "“$it”",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (detail.genres.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                detail.genres.forEach { SuggestionChip(onClick = {}, label = { Text(it) }) }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isFavorite: Boolean,
    hasTrailer: Boolean,
    onToggleFavorite: () -> Unit,
    onPlayTrailer: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isFavorite) {
            FilledTonalButton(onClick = onToggleFavorite, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.detail_in_favorites), Modifier.padding(start = 8.dp))
            }
        } else {
            Button(onClick = onToggleFavorite, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = null)
                Text(stringResource(R.string.detail_add_favorite), Modifier.padding(start = 8.dp))
            }
        }
        if (hasTrailer) {
            OutlinedButton(onClick = onPlayTrailer) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text(stringResource(R.string.detail_trailer), Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun NoteCard(note: String?, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(CoreUiR.string.note_heading),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp).weight(1f),
                )
                TextButton(onClick = onEdit) {
                    Text(stringResource(if (note.isNullOrBlank()) R.string.detail_note_add else R.string.detail_note_edit))
                }
            }
            Text(
                text = note?.takeIf { it.isNotBlank() } ?: stringResource(R.string.detail_note_empty),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = if (note.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
                color = if (note.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(top = 20.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
        )
        content()
    }
}

@Composable
private fun CastItem(cast: CastUi) {
    Column(Modifier.width(84.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        MoviePoster(url = cast.profileUrl, contentDescription = cast.name, shape = CircleShape, modifier = Modifier.size(72.dp))
        Text(
            cast.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        cast.character?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SimilarItem(movie: SimilarMovieUi, onClick: () -> Unit) {
    Column(Modifier.width(112.dp).clickable(onClick = onClick)) {
        Box {
            MoviePoster(
                url = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.width(112.dp).height(168.dp).sharedPoster(movie.id),
            )
            RatingBadge(movie.ratingLabel, Modifier.align(Alignment.BottomStart).padding(6.dp))
        }
        Text(
            movie.title,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/**
 * Round, translucent buttons floating over the backdrop: back on the left, share on the right.
 * Share only appears once the movie has loaded ([movieTitle] != null).
 */
@Composable
private fun HeaderButtons(
    movieTitle: String?,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
) {
    val overImage = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f), contentColor = Color.White)
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onBack, colors = overImage) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back))
        }
        if (movieTitle != null) {
            ShareMenuButton(movieTitle = movieTitle, onShare = onShare, onCopyLink = onCopyLink, colors = overImage)
        }
    }
}

@Composable
private fun DetailLoading() {
    Column {
        Box(Modifier.fillMaxWidth().height(260.dp).shimmer())
        Row(Modifier.padding(16.dp).offset(y = (-90).dp)) {
            Box(Modifier.width(120.dp).height(180.dp).clip(RoundedCornerShape(12.dp)).shimmer())
        }
        Column(Modifier.padding(horizontal = 16.dp).offset(y = (-70).dp)) {
            Box(Modifier.fillMaxWidth(0.6f).height(24.dp).clip(RoundedCornerShape(8.dp)).shimmer())
            Spacer(Modifier.height(12.dp))
            repeat(4) {
                Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(6.dp)).shimmer())
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ---------- Previews (light + dark) ----------

private val previewDetail = MovieDetailUi(
    id = 1,
    title = "Interstellar",
    tagline = "Mankind was born on Earth. It was never meant to die here.",
    overview = "The adventures of a group of explorers who make use of a newly discovered wormhole to surpass the " +
        "limitations on human space travel and conquer the vast distances involved in an interstellar voyage.",
    posterUrl = "preview",
    backdropUrl = "preview",
    yearLabel = "2014",
    ratingLabel = "8.5",
    voteCount = 36512,
    runtimeLabel = "2h 49m",
    genres = listOf("Adventure", "Drama", "Science Fiction"),
    cast = listOf(
        CastUi(1, "Matthew McConaughey", "Cooper", null),
        CastUi(2, "Anne Hathaway", "Brand", null),
        CastUi(3, "Jessica Chastain", "Murph", null),
    ),
    similar = listOf(
        SimilarMovieUi(10, "Gravity", "preview", "7.2"),
        SimilarMovieUi(11, "The Martian", "preview", "7.7"),
        SimilarMovieUi(12, "Arrival", null, "7.6"),
    ),
    trailerUrl = "https://www.youtube.com/watch?v=zSWdZVtXT7E",
)

@ScreenPreviews
@Composable
private fun DetailFavouriteWithNotePreview() = PreviewTheme {
    DetailScreen(
        state = DetailUiState(
            isLoading = false,
            detail = previewDetail,
            isFavorite = true,
            note = "Docking scene + Hans Zimmer organ = goosebumps. Rewatch on a big screen.",
        ),
        snackbarHostState = remember { SnackbarHostState() },
        onAction = {},
    )
}

@ScreenPreviews
@Composable
private fun DetailNotFavouritePreview() = PreviewTheme {
    DetailScreen(
        state = DetailUiState(isLoading = false, detail = previewDetail.copy(trailerUrl = null, tagline = null)),
        snackbarHostState = remember { SnackbarHostState() },
        onAction = {},
    )
}

@ThemePreviews
@Composable
private fun DetailLoadingPreview() = PreviewTheme {
    DetailScreen(state = DetailUiState(), snackbarHostState = remember { SnackbarHostState() }, onAction = {})
}

@ThemePreviews
@Composable
private fun DetailErrorPreview() = PreviewTheme {
    DetailScreen(
        state = DetailUiState(isLoading = false, error = AppError.NO_INTERNET),
        snackbarHostState = remember { SnackbarHostState() },
        onAction = {},
    )
}

@ThemePreviews
@Composable
private fun NoteCardPreview() = PreviewTheme {
    Column {
        NoteCard(note = "Docking scene = goosebumps.", onEdit = {})
        NoteCard(note = null, onEdit = {})
    }
}

@Composable
private fun noteLabels() = NoteEditorLabels(
    heading = stringResource(CoreUiR.string.note_heading),
    placeholder = stringResource(CoreUiR.string.note_placeholder),
    save = stringResource(CoreUiR.string.note_save),
    cancel = stringResource(CoreUiR.string.note_cancel),
)
