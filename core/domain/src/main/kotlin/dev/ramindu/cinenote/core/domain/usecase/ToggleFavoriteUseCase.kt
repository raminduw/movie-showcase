package dev.ramindu.cinenote.core.domain.usecase

import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.domain.model.MovieSnapshot
import dev.ramindu.cinenote.core.domain.repository.FavoritesReader
import dev.ramindu.cinenote.core.domain.repository.FavoritesWriter
import java.time.Clock
import javax.inject.Inject

/**
 * Adds the movie to favourites, or removes it if it is already there.
 * @return `true` if the movie is a favourite after the call.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val reader: FavoritesReader,
    private val writer: FavoritesWriter,
    private val clock: Clock,
) {
    suspend operator fun invoke(movie: MovieSnapshot): Boolean {
        return if (reader.getFavorite(movie.id) != null) {
            writer.remove(movie.id)
            false
        } else {
            writer.save(
                FavoriteMovie(movie = movie, note = null, addedAt = clock.instant(), noteUpdatedAt = null),
            )
            true
        }
    }
}
