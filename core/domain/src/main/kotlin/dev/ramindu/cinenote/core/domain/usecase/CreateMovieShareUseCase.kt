package dev.ramindu.cinenote.core.domain.usecase

import dev.ramindu.cinenote.core.domain.model.MovieShare
import dev.ramindu.cinenote.core.domain.model.MovieSnapshot
import javax.inject.Inject

/**
 * Builds the public link + details for sharing a movie.
 * Keeping the URL rule here means every screen shares the same canonical link.
 */
class CreateMovieShareUseCase @Inject constructor() {

    operator fun invoke(movie: MovieSnapshot): MovieShare = MovieShare(
        title = movie.title.trim(),
        releaseYear = movie.releaseYear,
        rating = movie.rating,
        url = movieUrl(movie.id),
    )

    companion object {
        private const val TMDB_MOVIE_URL = "https://www.themoviedb.org/movie/"

        fun movieUrl(movieId: Int): String = "$TMDB_MOVIE_URL$movieId"
    }
}
