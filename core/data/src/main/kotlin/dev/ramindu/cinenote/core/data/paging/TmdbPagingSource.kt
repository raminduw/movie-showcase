package dev.ramindu.cinenote.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.ramindu.cinenote.core.data.error.toAppError
import dev.ramindu.cinenote.core.data.mapper.toDomain
import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.result.AppException
import dev.ramindu.cinenote.core.network.dto.MovieDto
import dev.ramindu.cinenote.core.network.dto.PagedResponseDto
import kotlin.coroutines.cancellation.CancellationException

/**
 * Generic page loader for any TMDB list endpoint (Open/Closed: new lists just pass a new [fetchPage]).
 *
 * TMDB pages can repeat a movie (ranking shifts between requests). Duplicate keys crash LazyColumn,
 * so already-seen ids are dropped.
 */
class TmdbPagingSource(
    private val fetchPage: suspend (page: Int) -> PagedResponseDto<MovieDto>,
) : PagingSource<Int, Movie>() {

    private val seenIds = mutableSetOf<Int>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = fetchPage(page)
            if (params is LoadParams.Refresh) seenIds.clear()
            val movies = response.results
                .filter { seenIds.add(it.id) }
                .map { it.toDomain() }
            val lastPage = minOf(response.totalPages, MAX_TMDB_PAGE)
            LoadResult.Page(
                data = movies,
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = if (page >= lastPage) null else page + 1,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(AppException(e.toAppError(), e))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? =
        state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }

    companion object {
        const val FIRST_PAGE = 1

        /** TMDB rejects page > 500. */
        const val MAX_TMDB_PAGE = 500
    }
}
