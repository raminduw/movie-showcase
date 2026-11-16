package dev.ramindu.cinenote.core.domain.usecase

import androidx.paging.PagingData
import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.model.MovieCategory
import dev.ramindu.cinenote.core.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoviesByCategoryUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(category: MovieCategory): Flow<PagingData<Movie>> =
        repository.moviesByCategory(category)
}
