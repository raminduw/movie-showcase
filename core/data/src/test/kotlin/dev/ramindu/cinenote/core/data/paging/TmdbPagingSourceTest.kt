package dev.ramindu.cinenote.core.data.paging

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppException
import dev.ramindu.cinenote.core.network.dto.MovieDto
import dev.ramindu.cinenote.core.network.dto.PagedResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.IOException

class TmdbPagingSourceTest {

    private val requestedPages = mutableListOf<Int>()

    private fun source(totalPages: Int = 3, ids: (Int) -> List<Int> = { page -> listOf(page * 10, page * 10 + 1) }) =
        TmdbPagingSource { page ->
            requestedPages += page
            PagedResponseDto(page = page, results = ids(page).map { MovieDto(id = it, title = "M$it") }, totalPages = totalPages)
        }

    @Test
    fun `refresh loads the first page with no previous key`() = runTest {
        val result = source().load(LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))

        val page = result as LoadResult.Page
        assertThat(requestedPages).containsExactly(1)
        assertThat(page.data.map { it.id }).containsExactly(10, 11).inOrder()
        assertThat(page.prevKey).isNull()
        assertThat(page.nextKey).isEqualTo(2)
    }

    @Test
    fun `last page has no next key`() = runTest {
        val result = source(totalPages = 3).load(LoadParams.Append(key = 3, loadSize = 20, placeholdersEnabled = false))

        val page = result as LoadResult.Page
        assertThat(page.prevKey).isEqualTo(2)
        assertThat(page.nextKey).isNull()
    }

    @Test
    fun `stops at TMDB page limit even if more pages are reported`() = runTest {
        val result = source(totalPages = 10_000).load(
            LoadParams.Append(key = TmdbPagingSource.MAX_TMDB_PAGE, loadSize = 20, placeholdersEnabled = false),
        )

        assertThat((result as LoadResult.Page).nextKey).isNull()
    }

    @Test
    fun `drops movies already returned by an earlier page`() = runTest {
        val source = source(ids = { page -> if (page == 1) listOf(1, 2) else listOf(2, 3) })

        source.load(LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val second = source.load(LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false))

        assertThat((second as LoadResult.Page).data.map { it.id }).containsExactly(3)
    }

    @Test
    fun `network failure becomes an error carrying an AppError`() = runTest {
        val source = TmdbPagingSource { throw IOException("offline") }

        val result = source.load(LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))

        val error = (result as LoadResult.Error).throwable
        assertThat(error).isInstanceOf(AppException::class.java)
        assertThat((error as AppException).error).isEqualTo(AppError.NO_INTERNET)
    }
}
