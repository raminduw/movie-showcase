package dev.ramindu.cinenote.feature.favorites

import androidx.annotation.StringRes
import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.ui.Formatters

data class FavoriteUi(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val yearLabel: String,
    val ratingLabel: String,
    val note: String?,
)

fun FavoriteMovie.toUi(): FavoriteUi = FavoriteUi(
    id = movie.id,
    title = movie.title,
    posterUrl = movie.posterUrl,
    yearLabel = Formatters.year(movie.releaseYear),
    ratingLabel = Formatters.rating(movie.rating),
    note = note?.takeIf { it.isNotBlank() },
)

data class FavoritesUiState(
    val query: String = "",
    val items: List<FavoriteUi> = emptyList(),
    val isLoading: Boolean = true,
    val editingNoteFor: FavoriteUi? = null,
) {
    val isSearching: Boolean get() = query.isNotBlank()
}

sealed interface FavoritesAction {
    data class QueryChanged(val query: String) : FavoritesAction
    data class OpenMovie(val movieId: Int) : FavoritesAction
    data class Remove(val movieId: Int) : FavoritesAction
    data object UndoRemove : FavoritesAction
    data class EditNote(val movieId: Int) : FavoritesAction
    data class SaveNote(val text: String) : FavoritesAction
    data object DismissNoteEditor : FavoritesAction
    data object BrowseMovies : FavoritesAction
}

sealed interface FavoritesEffect {
    data class NavigateToDetail(val movieId: Int) : FavoritesEffect
    data object NavigateToMovies : FavoritesEffect
    data class ShowUndo(val movieTitle: String) : FavoritesEffect
    data class ShowMessage(@StringRes val messageRes: Int, val args: List<Any> = emptyList()) : FavoritesEffect
}
