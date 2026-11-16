package dev.ramindu.cinenote.core.data.repository

import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.data.mapper.toEntity
import dev.ramindu.cinenote.core.database.FavoriteDao
import dev.ramindu.cinenote.core.testing.TestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class OfflineFavoritesRepositoryTest {

    private val dao = mockk<FavoriteDao>(relaxUnitFun = true)
    private val repository = OfflineFavoritesRepository(dao)

    @Test
    fun `observeFavorites trims the query and maps entities`() = runTest {
        val favorite = TestData.favorite(id = 1, note = "hi")
        every { dao.observeFavorites("dune") } returns flowOf(listOf(favorite.toEntity()))

        val result = repository.observeFavorites("  dune ").first()

        assertThat(result).containsExactly(favorite)
    }

    @Test
    fun `observeFavoriteIds converts to a set and skips identical emissions`() = runTest {
        every { dao.observeIds() } returns flowOf(listOf(1, 2), listOf(2, 1), listOf(3))

        val emissions = repository.observeFavoriteIds().toList()

        assertThat(emissions).containsExactly(setOf(1, 2), setOf(3)).inOrder()
    }

    @Test
    fun `getFavorite returns null when the dao has nothing`() = runTest {
        coEvery { dao.getById(9) } returns null

        assertThat(repository.getFavorite(9)).isNull()
    }

    @Test
    fun `save upserts the mapped entity`() = runTest {
        val favorite = TestData.favorite(id = 4)

        repository.save(favorite)

        coVerify { dao.upsert(favorite.toEntity()) }
    }

    @Test
    fun `updateNote stores the timestamp as epoch millis`() = runTest {
        coEvery { dao.updateNote(any(), any(), any()) } returns 1

        repository.updateNote(4, "note", TestData.NOW)

        coVerify { dao.updateNote(4, "note", TestData.NOW.toEpochMilli()) }
    }

    @Test
    fun `remove deletes by id`() = runTest {
        repository.remove(4)

        coVerify { dao.deleteById(4) }
    }
}
