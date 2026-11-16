package dev.ramindu.cinenote.feature.favorites

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.domain.usecase.ObserveFavoritesUseCase
import dev.ramindu.cinenote.core.domain.usecase.RemoveFavoriteUseCase
import dev.ramindu.cinenote.core.domain.usecase.RestoreFavoriteUseCase
import dev.ramindu.cinenote.core.domain.usecase.UpdateFavoriteNoteUseCase
import dev.ramindu.cinenote.core.testing.FakeFavoritesRepository
import dev.ramindu.cinenote.core.testing.MainDispatcherExtension
import dev.ramindu.cinenote.core.testing.TestData
import dev.ramindu.cinenote.core.testing.TestData.NOW
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import dev.ramindu.cinenote.core.ui.R as CoreUiR

class FavoritesViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val repository = FakeFavoritesRepository(
        listOf(
            TestData.favorite(id = 1, title = "Dune", addedAt = NOW.minusSeconds(10)),
            TestData.favorite(id = 2, title = "Arrival", note = "Language as a weapon", addedAt = NOW),
        ),
    )

    private fun createViewModel() = FavoritesViewModel(
        observeFavorites = ObserveFavoritesUseCase(repository),
        removeFavorite = RemoveFavoriteUseCase(repository, repository),
        restoreFavorite = RestoreFavoriteUseCase(repository),
        updateNote = UpdateFavoriteNoteUseCase(repository, repository, TestData.fixedClock),
    )

    @Nested
    inner class Listing {
        @Test
        fun `loads favourites newest first`() = runTest {
            val viewModel = createViewModel()
            assertThat(viewModel.state.value.isLoading).isTrue()

            advanceUntilIdle()

            assertThat(viewModel.state.value.isLoading).isFalse()
            assertThat(viewModel.state.value.items.map { it.title }).containsExactly("Arrival", "Dune").inOrder()
        }

        @Test
        fun `search filters by title or note`() = runTest {
            val viewModel = createViewModel()

            viewModel.onAction(FavoritesAction.QueryChanged("weapon"))
            advanceUntilIdle()

            assertThat(viewModel.state.value.items.map { it.id }).containsExactly(2)
            assertThat(viewModel.state.value.isSearching).isTrue()
        }

        @Test
        fun `blank note is shown as no note`() = runTest {
            repository.save(TestData.favorite(id = 3, note = "  "))
            val viewModel = createViewModel()

            advanceUntilIdle()

            assertThat(viewModel.state.value.items.first { it.id == 3 }.note).isNull()
        }
    }

    @Nested
    inner class RemoveAndUndo {
        @Test
        fun `remove offers undo and undo restores the note too`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onAction(FavoritesAction.Remove(2))
                assertThat(awaitItem()).isEqualTo(FavoritesEffect.ShowUndo("Arrival"))
            }
            assertThat(viewModel.state.value.items.map { it.id }).containsExactly(1)

            viewModel.onAction(FavoritesAction.UndoRemove)
            advanceUntilIdle()

            assertThat(viewModel.state.value.items.map { it.id }).containsExactly(2, 1).inOrder()
            assertThat(repository.getFavorite(2)?.note).isEqualTo("Language as a weapon")
        }

        @Test
        fun `undo twice does not resurrect anything else`() = runTest {
            val viewModel = createViewModel()
            viewModel.onAction(FavoritesAction.Remove(1))
            advanceUntilIdle()
            viewModel.onAction(FavoritesAction.UndoRemove)
            advanceUntilIdle()
            repository.remove(1)

            viewModel.onAction(FavoritesAction.UndoRemove)
            advanceUntilIdle()

            assertThat(repository.getFavorite(1)).isNull()
        }
    }

    @Nested
    inner class Notes {
        @Test
        fun `edit opens the editor for that favourite`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onAction(FavoritesAction.EditNote(2))

            assertThat(viewModel.state.value.editingNoteFor?.title).isEqualTo("Arrival")
        }

        @Test
        fun `save stores the note and closes the editor`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onAction(FavoritesAction.EditNote(1))

            viewModel.effects.test {
                viewModel.onAction(FavoritesAction.SaveNote("Watch in IMAX"))
                assertThat(awaitItem()).isEqualTo(FavoritesEffect.ShowMessage(CoreUiR.string.note_saved))
            }
            assertThat(viewModel.state.value.editingNoteFor).isNull()
            assertThat(viewModel.state.value.items.first { it.id == 1 }.note).isEqualTo("Watch in IMAX")
        }

        @Test
        fun `saving blank text clears the note`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onAction(FavoritesAction.EditNote(2))

            viewModel.effects.test {
                viewModel.onAction(FavoritesAction.SaveNote(""))
                assertThat(awaitItem()).isEqualTo(FavoritesEffect.ShowMessage(CoreUiR.string.note_cleared))
            }
            assertThat(repository.getFavorite(2)?.note).isNull()
        }

        @Test
        fun `dismiss closes the editor without saving`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onAction(FavoritesAction.EditNote(1))

            viewModel.onAction(FavoritesAction.DismissNoteEditor)

            assertThat(viewModel.state.value.editingNoteFor).isNull()
            assertThat(repository.getFavorite(1)?.note).isNull()
        }
    }

    @Test
    fun `open and browse emit navigation effects`() = runTest {
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onAction(FavoritesAction.OpenMovie(2))
            assertThat(awaitItem()).isEqualTo(FavoritesEffect.NavigateToDetail(2))
            viewModel.onAction(FavoritesAction.BrowseMovies)
            assertThat(awaitItem()).isEqualTo(FavoritesEffect.NavigateToMovies)
        }
    }
}
