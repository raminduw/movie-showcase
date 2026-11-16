package dev.ramindu.cinenote.feature.detail

import androidx.annotation.StringRes
import dev.ramindu.cinenote.core.domain.model.MovieShare
import dev.ramindu.cinenote.core.domain.result.AppError

data class DetailUiState(
    val isLoading: Boolean = true,
    val detail: MovieDetailUi? = null,
    val error: AppError? = null,
    val isFavorite: Boolean = false,
    val note: String? = null,
    val isNoteEditorOpen: Boolean = false,
)

sealed interface DetailAction {
    data object Retry : DetailAction
    data object BackClicked : DetailAction
    data object ToggleFavorite : DetailAction
    data object PlayTrailer : DetailAction
    data object EditNote : DetailAction
    data object DismissNoteEditor : DetailAction
    data class SaveNote(val text: String) : DetailAction
    data class SimilarMovieClicked(val movieId: Int) : DetailAction
    data object Share : DetailAction
    data object CopyLink : DetailAction
}

sealed interface DetailEffect {
    data object NavigateBack : DetailEffect
    data class NavigateToMovie(val movieId: Int) : DetailEffect
    data class OpenUrl(val url: String) : DetailEffect
    data class ShowMessage(@StringRes val messageRes: Int, val args: List<Any> = emptyList()) : DetailEffect
    data class OpenShareSheet(val share: MovieShare) : DetailEffect
    data class CopyLink(val share: MovieShare) : DetailEffect
}
