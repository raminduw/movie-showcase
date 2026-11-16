package dev.ramindu.cinenote.feature.movies

import androidx.annotation.StringRes
import dev.ramindu.cinenote.core.domain.model.MovieCategory
import dev.ramindu.cinenote.core.domain.model.MovieShare

data class MoviesUiState(
    val query: String = "",
    val category: MovieCategory = MovieCategory.POPULAR,
    val favoriteIds: Set<Int> = emptySet(),
) {
    val isSearching: Boolean get() = query.isNotBlank()
}

sealed interface MoviesAction {
    data class QueryChanged(val query: String) : MoviesAction
    data class CategorySelected(val category: MovieCategory) : MoviesAction
    data class ToggleFavorite(val movie: MovieUi) : MoviesAction
    data class MovieClicked(val movieId: Int) : MoviesAction
    data class ShareMovie(val movie: MovieUi) : MoviesAction
    data class CopyMovieLink(val movie: MovieUi) : MoviesAction
}

sealed interface MoviesEffect {
    data class NavigateToDetail(val movieId: Int) : MoviesEffect
    data class ShowMessage(@StringRes val messageRes: Int, val movieTitle: String) : MoviesEffect
    /** Screen opens the system share sheet. */
    data class OpenShareSheet(val share: MovieShare) : MoviesEffect
    /** Screen copies [MovieShare.url] to the clipboard. */
    data class CopyLink(val share: MovieShare) : MoviesEffect
}
