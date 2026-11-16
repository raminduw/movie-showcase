package dev.ramindu.cinenote.core.domain.usecase

import androidx.paging.PagingData
import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** Searches the whole TMDB catalogue. A blank query never hits the network. */
class SearchMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(query: String): Flow<PagingData<Movie>> {
        val normalized = query.trim()
        return if (normalized.isEmpty()) {
            flowOf(PagingData.empty())
        } else {
            repository.searchMovies(normalized)
        }
    }
}
