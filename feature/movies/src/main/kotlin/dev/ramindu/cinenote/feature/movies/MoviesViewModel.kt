package dev.ramindu.cinenote.feature.movies

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ramindu.cinenote.core.domain.usecase.CreateMovieShareUseCase
import dev.ramindu.cinenote.core.domain.usecase.GetMoviesByCategoryUseCase
import dev.ramindu.cinenote.core.domain.usecase.ObserveFavoriteIdsUseCase
import dev.ramindu.cinenote.core.domain.usecase.SearchMoviesUseCase
import dev.ramindu.cinenote.core.domain.usecase.ToggleFavoriteUseCase
import dev.ramindu.cinenote.core.ui.mvi.MviViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getMoviesByCategory: GetMoviesByCategoryUseCase,
    private val searchMovies: SearchMoviesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val createMovieShare: CreateMovieShareUseCase,
    observeFavoriteIds: ObserveFavoriteIdsUseCase,
) : MviViewModel<MoviesUiState, MoviesAction, MoviesEffect>(MoviesUiState()) {

    /**
     * Search is debounced (typing shouldn't fire a request per key); clearing it is instant.
     * `flatMapLatest` cancels the previous request when the query/category changes.
     */
    val movies: Flow<PagingData<MovieUi>> = combine(
        state.map { it.query.trim() }.distinctUntilChanged().debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        state.map { it.category }.distinctUntilChanged(),
    ) { query, category -> query to category }
        .distinctUntilChanged()
        .flatMapLatest { (query, category) ->
            if (query.isEmpty()) getMoviesByCategory(category) else searchMovies(query)
        }
        .map { paging -> paging.map { it.toUi() } }
        .cachedIn(viewModelScope)

    init {
        observeFavoriteIds()
            .onEach { ids -> setState { copy(favoriteIds = ids) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: MoviesAction) {
        when (action) {
            is MoviesAction.QueryChanged -> setState { copy(query = action.query) }
            is MoviesAction.CategorySelected -> setState { copy(category = action.category) }
            is MoviesAction.MovieClicked -> sendEffect(MoviesEffect.NavigateToDetail(action.movieId))
            is MoviesAction.ShareMovie ->
                sendEffect(MoviesEffect.OpenShareSheet(createMovieShare(action.movie.toSnapshot())))
            is MoviesAction.CopyMovieLink ->
                sendEffect(MoviesEffect.CopyLink(createMovieShare(action.movie.toSnapshot())))
            is MoviesAction.ToggleFavorite -> viewModelScope.launch {
                val added = toggleFavorite(action.movie.toSnapshot())
                val message = if (added) R.string.movies_added_to_favorites else R.string.movies_removed_from_favorites
                sendEffect(MoviesEffect.ShowMessage(message, action.movie.title))
            }
        }
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
