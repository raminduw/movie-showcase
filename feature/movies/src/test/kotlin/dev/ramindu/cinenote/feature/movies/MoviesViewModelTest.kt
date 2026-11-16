package dev.ramindu.cinenote.feature.movies

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.domain.model.MovieCategory
import dev.ramindu.cinenote.core.domain.model.MovieShare
import dev.ramindu.cinenote.core.domain.usecase.CreateMovieShareUseCase
import dev.ramindu.cinenote.core.domain.usecase.GetMoviesByCategoryUseCase
import dev.ramindu.cinenote.core.domain.usecase.ObserveFavoriteIdsUseCase
import dev.ramindu.cinenote.core.domain.usecase.SearchMoviesUseCase
import dev.ramindu.cinenote.core.domain.usecase.ToggleFavoriteUseCase
import dev.ramindu.cinenote.core.testing.FakeFavoritesRepository
import dev.ramindu.cinenote.core.testing.FakeMovieRepository
import dev.ramindu.cinenote.core.testing.MainDispatcherExtension
import dev.ramindu.cinenote.core.testing.TestData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/** Uses real use cases over fake repositories – tests behaviour, not wiring. */
class MoviesViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val movieRepository = FakeMovieRepository().apply {
        moviesByCategory = mapOf(
            MovieCategory.POPULAR to listOf(TestData.movie(id = 1, title = "Popular One")),
            MovieCategory.TOP_RATED to listOf(TestData.movie(id = 2, title = "Top Rated One")),
        )
        searchResults = listOf(TestData.movie(id = 3, title = "Batman Begins"))
    }
    private val favoritesRepository = FakeFavoritesRepository()

    private fun createViewModel() = MoviesViewModel(
        getMoviesByCategory = GetMoviesByCategoryUseCase(movieRepository),
        searchMovies = SearchMoviesUseCase(movieRepository),
        toggleFavorite = ToggleFavoriteUseCase(favoritesRepository, favoritesRepository, TestData.fixedClock),
        createMovieShare = CreateMovieShareUseCase(),
        observeFavoriteIds = ObserveFavoriteIdsUseCase(favoritesRepository),
    )

    @Test
    fun `share opens the share sheet with the movie link`() = runTest {
        val viewModel = createViewModel()
        val movie = TestData.movie(id = 550, title = "Fight Club", rating = 8.4).toUi()

        viewModel.effects.test {
            viewModel.onAction(MoviesAction.ShareMovie(movie))

            assertThat(awaitItem()).isEqualTo(
                MoviesEffect.OpenShareSheet(
                    MovieShare(title = "Fight Club", releaseYear = 2024, rating = 8.4, url = "https://www.themoviedb.org/movie/550"),
                ),
            )
        }
    }

    @Test
    fun `copy link emits the same canonical link`() = runTest {
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onAction(MoviesAction.CopyMovieLink(TestData.movie(id = 7).toUi()))

            val effect = awaitItem() as MoviesEffect.CopyLink
            assertThat(effect.share.url).isEqualTo("https://www.themoviedb.org/movie/7")
        }
    }

    /**
     * `movies` is a never-ending, cached stream, so collect it in the background and
     * snapshot the most recent PagingData after the search debounce has elapsed.
     * (advanceUntilIdle() ignores backgroundScope work, hence advanceTimeBy + runCurrent.)
     */
    private fun TestScope.collectMovies(viewModel: MoviesViewModel): suspend () -> List<MovieUi> {
        var latest: PagingData<MovieUi>? = null
        backgroundScope.launch { viewModel.movies.collect { latest = it } }
        return {
            advanceTimeBy(MoviesViewModel.SEARCH_DEBOUNCE_MS + 1)
            runCurrent()
            flowOf(checkNotNull(latest) { "movies never emitted" }).asSnapshot()
        }
    }

    @Test
    fun `initially shows popular movies mapped to ui models`() = runTest {
        val viewModel = createViewModel()
        val snapshot = collectMovies(viewModel)

        val movies = snapshot()

        assertThat(movies.map { it.title }).containsExactly("Popular One")
        assertThat(movies.single().ratingLabel).isEqualTo("7.5")
        assertThat(movies.single().yearLabel).isEqualTo("2024")
    }

    @Test
    fun `selecting a category loads that category`() = runTest {
        val viewModel = createViewModel()
        val snapshot = collectMovies(viewModel)

        viewModel.onAction(MoviesAction.CategorySelected(MovieCategory.TOP_RATED))
        val movies = snapshot()

        assertThat(viewModel.state.value.category).isEqualTo(MovieCategory.TOP_RATED)
        assertThat(movies.map { it.title }).containsExactly("Top Rated One")
    }

    @Test
    fun `typing is debounced and only the final query hits TMDB`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.movies.collect {} }
        runCurrent()

        viewModel.onAction(MoviesAction.QueryChanged("b"))
        advanceTimeBy(100)
        viewModel.onAction(MoviesAction.QueryChanged("bat"))
        advanceTimeBy(100)
        viewModel.onAction(MoviesAction.QueryChanged("batman"))
        advanceTimeBy(MoviesViewModel.SEARCH_DEBOUNCE_MS + 1)
        runCurrent()

        assertThat(movieRepository.searchQueries).containsExactly("batman")
        assertThat(viewModel.state.value.isSearching).isTrue()
    }

    @Test
    fun `clearing the query goes back to the category list`() = runTest {
        val viewModel = createViewModel()
        val snapshot = collectMovies(viewModel)
        viewModel.onAction(MoviesAction.QueryChanged("batman"))
        assertThat(snapshot().map { it.title }).containsExactly("Batman Begins")

        viewModel.onAction(MoviesAction.QueryChanged(""))
        val movies = snapshot()

        assertThat(movies.map { it.title }).containsExactly("Popular One")
        assertThat(viewModel.state.value.isSearching).isFalse()
    }

    @Test
    fun `favourite ids from the repository are reflected in state`() = runTest {
        favoritesRepository.save(TestData.favorite(id = 1))
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertThat(viewModel.state.value.favoriteIds).containsExactly(1)
    }

    @Test
    fun `toggling favourite saves it and shows a confirmation`() = runTest {
        val viewModel = createViewModel()
        val movie = TestData.movie(id = 1, title = "Popular One").toUi()

        viewModel.effects.test {
            viewModel.onAction(MoviesAction.ToggleFavorite(movie))

            assertThat(awaitItem()).isEqualTo(MoviesEffect.ShowMessage(R.string.movies_added_to_favorites, "Popular One"))
            assertThat(favoritesRepository.current.map { it.id }).containsExactly(1)
            assertThat(viewModel.state.value.favoriteIds).containsExactly(1)
        }
    }

    @Test
    fun `toggling an existing favourite removes it`() = runTest {
        favoritesRepository.save(TestData.favorite(id = 1))
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onAction(MoviesAction.ToggleFavorite(TestData.movie(id = 1).toUi()))

            assertThat((awaitItem() as MoviesEffect.ShowMessage).messageRes).isEqualTo(R.string.movies_removed_from_favorites)
            assertThat(favoritesRepository.current).isEmpty()
        }
    }

    @Test
    fun `clicking a movie navigates to its detail`() = runTest {
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onAction(MoviesAction.MovieClicked(42))
            assertThat(awaitItem()).isEqualTo(MoviesEffect.NavigateToDetail(42))
        }
    }
}
