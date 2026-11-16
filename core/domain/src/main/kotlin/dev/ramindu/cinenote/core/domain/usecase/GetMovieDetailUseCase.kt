package dev.ramindu.cinenote.core.domain.usecase

import dev.ramindu.cinenote.core.domain.model.MovieDetail
import dev.ramindu.cinenote.core.domain.repository.MovieRepository
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppResult
import javax.inject.Inject

class GetMovieDetailUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(movieId: Int): AppResult<MovieDetail> =
        if (movieId <= 0) AppResult.Failure(AppError.NOT_FOUND) else repository.movieDetail(movieId)
}
