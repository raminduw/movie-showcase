package dev.ramindu.cinenote.core.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.testing.FakeFavoritesRepository
import dev.ramindu.cinenote.core.testing.TestData
import dev.ramindu.cinenote.core.testing.TestData.NOW
import dev.ramindu.cinenote.core.testing.TestData.fixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FavoriteUseCasesTest {

    @Nested
    inner class ToggleFavorite {
        private val repository = FakeFavoritesRepository()
        private val useCase = ToggleFavoriteUseCase(repository, repository, fixedClock)

        @Test
        fun `adds a movie that is not a favourite yet`() = runTest {
            val added = useCase(TestData.snapshot(id = 5))

            assertThat(added).isTrue()
            val saved = repository.getFavorite(5)!!
            assertThat(saved.addedAt).isEqualTo(NOW)
            assertThat(saved.note).isNull()
        }

        @Test
        fun `removes a movie that is already a favourite`() = runTest {
            repository.save(TestData.favorite(id = 5))

            val added = useCase(TestData.snapshot(id = 5))

            assertThat(added).isFalse()
            assertThat(repository.getFavorite(5)).isNull()
        }
    }

    @Nested
    inner class RemoveAndRestore {
        private val original = TestData.favorite(id = 2, note = "Watch again")
        private val repository = FakeFavoritesRepository(listOf(original))

        @Test
        fun `remove returns the removed favourite including its note`() = runTest {
            val removed = RemoveFavoriteUseCase(repository, repository)(2)

            assertThat(removed).isEqualTo(original)
            assertThat(repository.current).isEmpty()
        }

        @Test
        fun `remove of unknown id returns null and changes nothing`() = runTest {
            val removed = RemoveFavoriteUseCase(repository, repository)(404)

            assertThat(removed).isNull()
            assertThat(repository.current).containsExactly(original)
        }

        @Test
        fun `restore puts the favourite back unchanged`() = runTest {
            val removed = RemoveFavoriteUseCase(repository, repository)(2)!!

            RestoreFavoriteUseCase(repository)(removed)

            assertThat(repository.current).containsExactly(original)
        }
    }

    @Nested
    inner class UpdateNote {
        private val repository = FakeFavoritesRepository(listOf(TestData.favorite(id = 1)))
        private val useCase = UpdateFavoriteNoteUseCase(repository, repository, fixedClock)

        @Test
        fun `saves a trimmed note with a timestamp`() = runTest {
            val result = useCase(1, "  Great soundtrack  ")

            assertThat(result).isEqualTo(NoteUpdateResult.Saved)
            val fav = repository.getFavorite(1)!!
            assertThat(fav.note).isEqualTo("Great soundtrack")
            assertThat(fav.noteUpdatedAt).isEqualTo(NOW)
        }

        @Test
        fun `blank note clears the existing note`() = runTest {
            useCase(1, "Something")

            val result = useCase(1, "   ")

            assertThat(result).isEqualTo(NoteUpdateResult.Cleared)
            assertThat(repository.getFavorite(1)!!.note).isNull()
            assertThat(repository.getFavorite(1)!!.noteUpdatedAt).isNull()
        }

        @Test
        fun `note longer than the limit is rejected`() = runTest {
            val tooLong = "a".repeat(UpdateFavoriteNoteUseCase.MAX_NOTE_LENGTH + 1)

            val result = useCase(1, tooLong)

            assertThat(result).isEqualTo(NoteUpdateResult.TooLong(UpdateFavoriteNoteUseCase.MAX_NOTE_LENGTH))
            assertThat(repository.getFavorite(1)!!.note).isNull()
        }

        @Test
        fun `note exactly at the limit is accepted`() = runTest {
            val result = useCase(1, "a".repeat(UpdateFavoriteNoteUseCase.MAX_NOTE_LENGTH))

            assertThat(result).isEqualTo(NoteUpdateResult.Saved)
        }

        @Test
        fun `note on a movie that is not a favourite is rejected`() = runTest {
            val result = useCase(42, "note")

            assertThat(result).isEqualTo(NoteUpdateResult.NotAFavorite)
        }
    }

    @Nested
    inner class Observe {
        private val repository = FakeFavoritesRepository(
            listOf(
                TestData.favorite(id = 1, title = "Dune", addedAt = NOW.minusSeconds(60)),
                TestData.favorite(id = 2, title = "Alien", note = "Scary but brilliant", addedAt = NOW),
            ),
        )

        @Test
        fun `emits newest favourite first`() = runTest {
            val list = ObserveFavoritesUseCase(repository)().first()

            assertThat(list.map { it.id }).containsExactly(2, 1).inOrder()
        }

        @Test
        fun `query is trimmed and matches the note text`() = runTest {
            val list = ObserveFavoritesUseCase(repository)("  brilliant ").first()

            assertThat(list.map { it.id }).containsExactly(2)
        }

        @Test
        fun `ids stream updates when favourites change`() = runTest {
            ObserveFavoriteIdsUseCase(repository)().test {
                assertThat(awaitItem()).containsExactly(1, 2)
                repository.remove(1)
                assertThat(awaitItem()).containsExactly(2)
            }
        }

        @Test
        fun `single favourite stream emits null after removal`() = runTest {
            ObserveFavoriteUseCase(repository)(1).test {
                assertThat(awaitItem()?.id).isEqualTo(1)
                repository.remove(1)
                assertThat(awaitItem()).isNull()
            }
        }
    }
}
