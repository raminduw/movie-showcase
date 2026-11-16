package dev.ramindu.cinenote.feature.favorites

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.domain.usecase.NoteUpdateResult
import dev.ramindu.cinenote.core.domain.usecase.ObserveFavoritesUseCase
import dev.ramindu.cinenote.core.domain.usecase.RemoveFavoriteUseCase
import dev.ramindu.cinenote.core.domain.usecase.RestoreFavoriteUseCase
import dev.ramindu.cinenote.core.domain.usecase.UpdateFavoriteNoteUseCase
import dev.ramindu.cinenote.core.ui.mvi.MviViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.ramindu.cinenote.core.ui.R as CoreUiR

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavorites: ObserveFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
    private val restoreFavorite: RestoreFavoriteUseCase,
    private val updateNote: UpdateFavoriteNoteUseCase,
) : MviViewModel<FavoritesUiState, FavoritesAction, FavoritesEffect>(FavoritesUiState()) {

    /** Kept so "Undo" can restore the favourite *with* its note. */
    private var lastRemoved: FavoriteMovie? = null

    init {
        // Local search is cheap (Room), so no debounce – results update as you type.
        state.map { it.query.trim() }
            .distinctUntilChanged()
            .flatMapLatest { query -> observeFavorites(query) }
            .onEach { favorites -> setState { copy(items = favorites.map { it.toUi() }, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: FavoritesAction) {
        when (action) {
            is FavoritesAction.QueryChanged -> setState { copy(query = action.query) }
            is FavoritesAction.OpenMovie -> sendEffect(FavoritesEffect.NavigateToDetail(action.movieId))
            FavoritesAction.BrowseMovies -> sendEffect(FavoritesEffect.NavigateToMovies)
            is FavoritesAction.Remove -> viewModelScope.launch {
                removeFavorite(action.movieId)?.let { removed ->
                    lastRemoved = removed
                    sendEffect(FavoritesEffect.ShowUndo(removed.movie.title))
                }
            }
            FavoritesAction.UndoRemove -> viewModelScope.launch {
                lastRemoved?.let { restoreFavorite(it) }
                lastRemoved = null
            }
            is FavoritesAction.EditNote -> setState {
                copy(editingNoteFor = items.firstOrNull { it.id == action.movieId })
            }
            FavoritesAction.DismissNoteEditor -> setState { copy(editingNoteFor = null) }
            is FavoritesAction.SaveNote -> saveNote(action.text)
        }
    }

    private fun saveNote(text: String) {
        val target = currentState.editingNoteFor ?: return
        viewModelScope.launch {
            when (val result = updateNote(target.id, text)) {
                NoteUpdateResult.Saved -> closeEditorWith(CoreUiR.string.note_saved)
                NoteUpdateResult.Cleared -> closeEditorWith(CoreUiR.string.note_cleared)
                is NoteUpdateResult.TooLong ->
                    sendEffect(FavoritesEffect.ShowMessage(CoreUiR.string.note_too_long, listOf(result.maxLength)))
                NoteUpdateResult.NotAFavorite -> setState { copy(editingNoteFor = null) }
            }
        }
    }

    private fun closeEditorWith(messageRes: Int) {
        setState { copy(editingNoteFor = null) }
        sendEffect(FavoritesEffect.ShowMessage(messageRes))
    }
}
