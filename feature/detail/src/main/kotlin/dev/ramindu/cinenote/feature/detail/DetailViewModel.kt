package dev.ramindu.cinenote.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ramindu.cinenote.core.domain.model.MovieDetail
import dev.ramindu.cinenote.core.domain.model.toSnapshot
import dev.ramindu.cinenote.core.domain.result.AppResult
import dev.ramindu.cinenote.core.domain.usecase.CreateMovieShareUseCase
import dev.ramindu.cinenote.core.domain.usecase.GetMovieDetailUseCase
import dev.ramindu.cinenote.core.domain.usecase.NoteUpdateResult
import dev.ramindu.cinenote.core.domain.usecase.ObserveFavoriteUseCase
import dev.ramindu.cinenote.core.domain.usecase.ToggleFavoriteUseCase
import dev.ramindu.cinenote.core.domain.usecase.UpdateFavoriteNoteUseCase
import dev.ramindu.cinenote.core.ui.mvi.MviViewModel
import dev.ramindu.cinenote.feature.detail.navigation.MovieDetailDestination
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.ramindu.cinenote.core.ui.R as CoreUiR

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetail: GetMovieDetailUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val updateNote: UpdateFavoriteNoteUseCase,
    private val createMovieShare: CreateMovieShareUseCase,
    observeFavorite: ObserveFavoriteUseCase,
) : MviViewModel<DetailUiState, DetailAction, DetailEffect>(DetailUiState()) {

    /** Navigation stores the route's properties in SavedStateHandle under their names. */
    private val movieId: Int = checkNotNull(savedStateHandle[MovieDetailDestination.ARG_MOVIE_ID])

    private var loadedDetail: MovieDetail? = null

    init {
        load()
        observeFavorite(movieId)
            .onEach { favorite -> setState { copy(isFavorite = favorite != null, note = favorite?.note) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: DetailAction) {
        when (action) {
            DetailAction.Retry -> load()
            DetailAction.BackClicked -> sendEffect(DetailEffect.NavigateBack)
            DetailAction.ToggleFavorite -> onToggleFavorite()
            DetailAction.PlayTrailer -> currentState.detail?.trailerUrl?.let { sendEffect(DetailEffect.OpenUrl(it)) }
            DetailAction.EditNote -> if (currentState.isFavorite) setState { copy(isNoteEditorOpen = true) }
            DetailAction.DismissNoteEditor -> setState { copy(isNoteEditorOpen = false) }
            is DetailAction.SaveNote -> onSaveNote(action.text)
            is DetailAction.SimilarMovieClicked -> sendEffect(DetailEffect.NavigateToMovie(action.movieId))
            // Sharing needs the loaded title/year/rating; the button is only shown once loaded.
            DetailAction.Share -> loadedDetail?.let {
                sendEffect(DetailEffect.OpenShareSheet(createMovieShare(it.movie.toSnapshot())))
            }
            DetailAction.CopyLink -> loadedDetail?.let {
                sendEffect(DetailEffect.CopyLink(createMovieShare(it.movie.toSnapshot())))
            }
        }
    }

    private fun load() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getMovieDetail(movieId)) {
                is AppResult.Success -> {
                    loadedDetail = result.data
                    setState { copy(isLoading = false, detail = result.data.toUi(), error = null) }
                }
                is AppResult.Failure -> setState { copy(isLoading = false, error = result.error) }
            }
        }
    }

    private fun onToggleFavorite() {
        val detail = loadedDetail ?: return
        viewModelScope.launch {
            val added = toggleFavorite(detail.movie.toSnapshot())
            sendEffect(
                DetailEffect.ShowMessage(if (added) R.string.detail_added_to_favorites else R.string.detail_removed_from_favorites),
            )
        }
    }

    private fun onSaveNote(text: String) {
        viewModelScope.launch {
            when (val result = updateNote(movieId, text)) {
                NoteUpdateResult.Saved -> {
                    setState { copy(isNoteEditorOpen = false) }
                    sendEffect(DetailEffect.ShowMessage(CoreUiR.string.note_saved))
                }
                NoteUpdateResult.Cleared -> {
                    setState { copy(isNoteEditorOpen = false) }
                    sendEffect(DetailEffect.ShowMessage(CoreUiR.string.note_cleared))
                }
                is NoteUpdateResult.TooLong ->
                    sendEffect(DetailEffect.ShowMessage(CoreUiR.string.note_too_long, listOf(result.maxLength)))
                NoteUpdateResult.NotAFavorite -> setState { copy(isNoteEditorOpen = false) }
            }
        }
    }
}
