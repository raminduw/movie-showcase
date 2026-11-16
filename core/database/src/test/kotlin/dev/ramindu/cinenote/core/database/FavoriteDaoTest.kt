package dev.ramindu.cinenote.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Runs real SQL against an in-memory SQLite database on the JVM (Robolectric). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FavoriteDaoTest {

    private lateinit var database: CineNoteDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CineNoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.favoriteDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun upsert_thenGetById_returnsEntity() = runTest {
        val entity = entity(id = 1)

        dao.upsert(entity)

        assertThat(dao.getById(1)).isEqualTo(entity)
    }

    @Test
    fun upsert_existingId_replacesRow() = runTest {
        dao.upsert(entity(id = 1, title = "Old"))

        dao.upsert(entity(id = 1, title = "New"))

        assertThat(dao.getById(1)?.title).isEqualTo("New")
        assertThat(dao.observeIds().first()).containsExactly(1)
    }

    @Test
    fun observeFavorites_emptyQuery_returnsNewestFirst() = runTest {
        dao.upsert(entity(id = 1, addedAt = 100))
        dao.upsert(entity(id = 2, addedAt = 300))
        dao.upsert(entity(id = 3, addedAt = 200))

        val ids = dao.observeFavorites("").first().map { it.movieId }

        assertThat(ids).containsExactly(2, 3, 1).inOrder()
    }

    @Test
    fun observeFavorites_query_matchesTitleOrNoteCaseInsensitive() = runTest {
        dao.upsert(entity(id = 1, title = "Dune: Part Two"))
        dao.upsert(entity(id = 2, title = "Alien", note = "Rewatch with DUNE fans"))
        dao.upsert(entity(id = 3, title = "Heat", note = "Bank scene"))

        val ids = dao.observeFavorites("dune").first().map { it.movieId }

        assertThat(ids).containsExactly(1, 2)
    }

    @Test
    fun deleteById_removesRow_andIdStreamUpdates() = runTest {
        dao.upsert(entity(id = 1))
        dao.upsert(entity(id = 2))

        dao.observeIds().test {
            assertThat(awaitItem()).containsExactly(1, 2)
            dao.deleteById(1)
            assertThat(awaitItem()).containsExactly(2)
        }
    }

    @Test
    fun updateNote_existingRow_updatesNoteAndTimestamp() = runTest {
        dao.upsert(entity(id = 1))

        val updated = dao.updateNote(1, note = "Loved it", updatedAtEpochMillis = 999)

        assertThat(updated).isEqualTo(1)
        with(dao.getById(1)!!) {
            assertThat(note).isEqualTo("Loved it")
            assertThat(noteUpdatedAtEpochMillis).isEqualTo(999)
        }
    }

    @Test
    fun updateNote_missingRow_updatesNothing() = runTest {
        val updated = dao.updateNote(42, note = "x", updatedAtEpochMillis = 1)

        assertThat(updated).isEqualTo(0)
    }

    @Test
    fun observeById_emitsNullAfterDelete() = runTest {
        dao.upsert(entity(id = 7))

        dao.observeById(7).test {
            assertThat(awaitItem()?.movieId).isEqualTo(7)
            dao.deleteById(7)
            assertThat(awaitItem()).isNull()
        }
    }

    private fun entity(
        id: Int,
        title: String = "Movie $id",
        note: String? = null,
        addedAt: Long = 1_000L * id,
    ) = FavoriteEntity(
        movieId = id,
        title = title,
        posterUrl = "https://image.tmdb.org/t/p/w500/p$id.jpg",
        releaseYear = 2024,
        rating = 7.0,
        addedAtEpochMillis = addedAt,
        note = note,
        noteUpdatedAtEpochMillis = null,
    )
}
