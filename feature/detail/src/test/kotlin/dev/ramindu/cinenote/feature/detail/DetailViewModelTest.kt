package dev.ramindu.cinenote.feature.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.domain.model.MovieShare
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppResult
import dev.ramindu.cinenote.core.domain.usecase.CreateMovieShareUseCase
import dev.ramindu.cinenote.core.domain.usecase.GetMovieDetailUseCase
import dev.ramindu.cinenote.core.domain.usecase.ObserveFavoriteUseCase
import dev.ramindu.cinenote.core.domain.usecase.ToggleFavoriteUseCase
import dev.ramindu.cinenote.core.domain.usecase.UpdateFavoriteNoteUseCase
import dev.ramindu.cinenote.core.testing.FakeFavoritesRepository
import dev.ramindu.cinenote.core.testing.FakeMovieRepository
import dev.ramindu.cinenote.core.testing.MainDispatcherExtension
import dev.ramindu.cinenote.core.testing.TestData
import dev.ramindu.cinenote.feature.detail.navigation.MovieDetailDestination
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import dev.ramindu.cinenote.core.ui.R as CoreUiR

class DetailViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val movie = TestData.movie(id = MOVIE_ID, title = "Interstellar")
    private val movieRepository = FakeMovieRepository().apply {
        detailResult = AppResult.Success(TestData.movieDetail(movie = movie))
    }
    private val favorites = FakeFavoritesRepository()

    private fun createViewModel() = DetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(MovieDetailDestination.ARG_MOVIE_ID to MOVIE_ID)),
        getMovieDetail = GetMovieDetailUseCase(movieRepository),
        toggleFavorite = ToggleFavoriteUseCase(favorites, favorites, TestData.fixedClock),
        updateNote = UpdateFavoriteNoteUseCase(favorites, favorites, TestData.fixedClock),
        createMovieShare = CreateMovieShareUseCase(),
        observeFavorite = ObserveFavoriteUseCase(favorites),
    )

    @Nested
    inner class Sharing {
        @Test
        fun `share opens the share sheet with the loaded movie and its link`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onAction(DetailAction.Share)

                assertThat(awaitItem()).isEqualTo(
                    DetailEffect.OpenShareSheet(
                        MovieShare(
                            title = "Interstellar",
                            releaseYear = 2024,
                            rating = 7.5,
                            url = "https://www.themoviedb.org/movie/$MOVIE_ID",
                        ),
                    ),
                )
            }
        }

        @Test
        fun `copy link emits the movie link`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onAction(DetailAction.CopyLink)

                assertThat((awaitItem() as DetailEffect.CopyLink).share.url)
                    .isEqualTo("https://www.themoviedb.org/movie/$MOVIE_ID")
            }
        }

        @Test
        fun `share does nothing until the detail has loaded`() = runTest {
            movieRepository.detailResult = AppResult.Failure(AppError.SERVER)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onAction(DetailAction.Share)
                viewModel.onAction(DetailAction.CopyLink)
                advanceUntilIdle()

                expectNoEvents()
            }
        }
    }

    @Nested
    inner class Loading {
        @Test
        fun `starts loading then shows the mapped detail`() = runTest {
            val viewModel = createViewModel()
            assertThat(viewModel.state.value.isLoading).isTrue()

            advanceUntilIdle()

            with(viewModel.state.value) {
                assertThat(isLoading).isFalse()
                assertThat(error).isNull()
                assertThat(detail?.title).isEqualTo("Interstellar")
                assertThat(detail?.runtimeLabel).isEqualTo("2h 5m")
                assertThat(detail?.genres).containsExactly("Action", "Adventure").inOrder()
            }
            assertThat(movieRepository.detailRequests).containsExactly(MOVIE_ID)
        }

        @Test
        fun `failure shows the error and retry reloads`() = runTest {
            movieRepository.detailResult = AppResult.Failure(AppError.NO_INTERNET)
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertThat(viewModel.state.value.error).isEqualTo(AppError.NO_INTERNET)

            movieRepository.detailResult = AppResult.Success(TestData.movieDetail(movie = movie))
            viewModel.onAction(DetailAction.Retry)
            advanceUntilIdle()

            assertThat(viewModel.state.value.error).isNull()
            assertThat(viewModel.state.value.detail).isNotNull()
            assertThat(movieRepository.detailRequests).hasSize(2)
        }
    }

    @Nested
    inner class Favourites {
        @Test
        fun `reflects an existing favourite and its note`() = runTest {
            favorites.save(TestData.favorite(id = MOVIE_ID, note = "Cry every time"))
            val viewModel = createViewModel()

            advanceUntilIdle()

            assertThat(viewModel.state.value.isFavorite).isTrue()
            assertThat(viewModel.state.value.note).isEqualTo("Cry every time")
        }

        @Test
        fun `toggle adds the loaded movie and confirms`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onAction(DetailAction.ToggleFavorite)
                assertThat(awaitItem()).isEqualTo(DetailEffect.ShowMessage(R.string.detail_added_to_favorites))
            }
            assertThat(viewModel.state.value.isFavorite).isTrue()
            assertThat(favorites.getFavorite(MOVIE_ID)?.movie?.title).isEqualTo("Interstellar")
        }

        @Test
        fun `toggle does nothing before the detail has loaded`() = runTest {
            movieRepository.detailResult = AppResult.Failure(AppError.SERVER)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onAction(DetailAction.ToggleFavorite)
            advanceUntilIdle()

            assertThat(favorites.current).isEmpty()
        }
    }

    @Nested
    inner class Notes {
        @Test
        fun `edit note only opens for favourites`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onAction(DetailAction.EditNote)
            assertThat(viewModel.state.value.isNoteEditorOpen).isFalse()

            favorites.save(TestData.favorite(id = MOVIE_ID))
            advanceUntilIdle()
            viewModel.onAction(DetailAction.EditNote)
            assertThat(viewModel.state.value.isNoteEditorOpen).isTrue()
        }

        @Test
        fun `saving a note stores it, closes the editor and confirms`() = runTest {
            favorites.save(TestData.favorite(id = MOVIE_ID))
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onAction(DetailAction.EditNote)

            viewModel.effects.test {
                viewModel.onAction(DetailAction.SaveNote("Docking scene!"))
                assertThat(awaitItem()).isEqualTo(DetailEffect.ShowMessage(CoreUiR.string.note_saved))
            }
            assertThat(viewModel.state.value.isNoteEditorOpen).isFalse()
            assertThat(viewModel.state.value.note).isEqualTo("Docking scene!")
        }

        @Test
        fun `too long note keeps the editor open and explains why`() = runTest {
            favorites.save(TestData.favorite(id = MOVIE_ID))
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onAction(DetailAction.EditNote)

            viewModel.effects.test {
                viewModel.onAction(DetailAction.SaveNote("x".repeat(UpdateFavoriteNoteUseCase.MAX_NOTE_LENGTH + 1)))
                assertThat(awaitItem()).isEqualTo(
                    DetailEffect.ShowMessage(CoreUiR.string.note_too_long, listOf(UpdateFavoriteNoteUseCase.MAX_NOTE_LENGTH)),
                )
            }
            assertThat(viewModel.state.value.isNoteEditorOpen).isTrue()
        }
    }

    @Nested
    inner class Navigation {
        @Test
        fun `back, similar movie and trailer emit effects`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onAction(DetailAction.BackClicked)
                assertThat(awaitItem()).isEqualTo(DetailEffect.NavigateBack)

                viewModel.onAction(DetailAction.SimilarMovieClicked(99))
                assertThat(awaitItem()).isEqualTo(DetailEffect.NavigateToMovie(99))

                viewModel.onAction(DetailAction.PlayTrailer)
                assertThat(awaitItem()).isEqualTo(DetailEffect.OpenUrl("https://www.youtube.com/watch?v=abc123"))
            }
        }
    }

    private companion object {
        const val MOVIE_ID = 157336
    }
}
