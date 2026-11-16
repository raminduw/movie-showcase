package dev.ramindu.cinenote.core.domain.usecase

import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.domain.repository.FavoritesReader
import dev.ramindu.cinenote.core.domain.repository.FavoritesWriter
import javax.inject.Inject

/**
 * Removes a favourite and returns what was removed (note included),
 * so the caller can offer "Undo" via [RestoreFavoriteUseCase].
 */
class RemoveFavoriteUseCase @Inject constructor(
    private val reader: FavoritesReader,
    private val writer: FavoritesWriter,
) {
    suspend operator fun invoke(movieId: Int): FavoriteMovie? {
        val existing = reader.getFavorite(movieId) ?: return null
        writer.remove(movieId)
        return existing
    }
}

/** Puts a previously removed favourite back exactly as it was. */
class RestoreFavoriteUseCase @Inject constructor(
    private val writer: FavoritesWriter,
) {
    suspend operator fun invoke(favorite: FavoriteMovie) = writer.save(favorite)
}
