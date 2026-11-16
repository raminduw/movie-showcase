package dev.ramindu.cinenote.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.ramindu.cinenote.core.data.error.safeCall
import dev.ramindu.cinenote.core.data.mapper.toDomain
import dev.ramindu.cinenote.core.data.paging.TmdbPagingSource
import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.model.MovieCategory
import dev.ramindu.cinenote.core.domain.model.MovieDetail
import dev.ramindu.cinenote.core.domain.repository.MovieRepository
import dev.ramindu.cinenote.core.domain.result.AppResult
import dev.ramindu.cinenote.core.network.TmdbApi
import dev.ramindu.cinenote.core.network.dto.MovieDto
import dev.ramindu.cinenote.core.network.dto.PagedResponseDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TmdbMovieRepository @Inject constructor(
    private val api: TmdbApi,
) : MovieRepository {

    override fun moviesByCategory(category: MovieCategory): Flow<PagingData<Movie>> = pager { page ->
        when (category) {
            MovieCategory.TRENDING -> api.trendingMovies(page)
            else -> api.movieList(list = category.toListPath(), page = page)
        }
    }

    override fun searchMovies(query: String): Flow<PagingData<Movie>> = pager { page ->
        api.searchMovies(query = query, page = page)
    }

    override suspend fun movieDetail(movieId: Int): AppResult<MovieDetail> = safeCall {
        api.movieDetail(movieId).toDomain()
    }

    private fun pager(fetch: suspend (page: Int) -> PagedResponseDto<MovieDto>): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { TmdbPagingSource(fetch) },
        ).flow

    companion object {
        /** TMDB always returns 20 results per page. */
        const val PAGE_SIZE = 20
    }
}

internal fun MovieCategory.toListPath(): String = when (this) {
    MovieCategory.POPULAR -> "popular"
    MovieCategory.TOP_RATED -> "top_rated"
    MovieCategory.NOW_PLAYING -> "now_playing"
    MovieCategory.UPCOMING -> "upcoming"
    MovieCategory.TRENDING -> error("Trending has its own endpoint")
}
