package dev.ramindu.cinenote.core.testing

import androidx.paging.PagingData
import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.model.MovieCategory
import dev.ramindu.cinenote.core.domain.model.MovieDetail
import dev.ramindu.cinenote.core.domain.repository.MovieRepository
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Scriptable movie source that records how it was called. */
class FakeMovieRepository : MovieRepository {

    var moviesByCategory: Map<MovieCategory, List<Movie>> = emptyMap()
    var searchResults: List<Movie> = emptyList()
    var detailResult: AppResult<MovieDetail> = AppResult.Failure(AppError.NOT_FOUND)

    val requestedCategories = mutableListOf<MovieCategory>()
    val searchQueries = mutableListOf<String>()
    val detailRequests = mutableListOf<Int>()

    override fun moviesByCategory(category: MovieCategory): Flow<PagingData<Movie>> {
        requestedCategories += category
        return flowOf(PagingData.from(moviesByCategory[category].orEmpty()))
    }

    override fun searchMovies(query: String): Flow<PagingData<Movie>> {
        searchQueries += query
        return flowOf(PagingData.from(searchResults))
    }

    override suspend fun movieDetail(movieId: Int): AppResult<MovieDetail> {
        detailRequests += movieId
        return detailResult
    }
}
