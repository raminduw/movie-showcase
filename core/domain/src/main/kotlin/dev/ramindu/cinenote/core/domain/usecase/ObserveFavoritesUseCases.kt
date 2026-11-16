package dev.ramindu.cinenote.core.domain.usecase

import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.domain.repository.FavoritesReader
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Favourites list, optionally filtered by title or note text. */
class ObserveFavoritesUseCase @Inject constructor(
    private val reader: FavoritesReader,
) {
    operator fun invoke(query: String = ""): Flow<List<FavoriteMovie>> =
        reader.observeFavorites(query.trim())
}

/** Ids only – lets list screens draw the heart state cheaply. */
class ObserveFavoriteIdsUseCase @Inject constructor(
    private val reader: FavoritesReader,
) {
    operator fun invoke(): Flow<Set<Int>> = reader.observeFavoriteIds()
}

/** A single favourite (or null) – used by the detail screen for heart + note. */
class ObserveFavoriteUseCase @Inject constructor(
    private val reader: FavoritesReader,
) {
    operator fun invoke(movieId: Int): Flow<FavoriteMovie?> = reader.observeFavorite(movieId)
}
