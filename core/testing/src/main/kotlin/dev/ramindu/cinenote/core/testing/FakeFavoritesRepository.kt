package dev.ramindu.cinenote.core.testing

import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

/** In-memory favourites with the same observable behaviour as the Room-backed repository. */
class FakeFavoritesRepository(initial: List<FavoriteMovie> = emptyList()) : FavoritesRepository {

    private val store = MutableStateFlow(initial.associateBy { it.id })

    val current: List<FavoriteMovie> get() = store.value.values.toList()

    override fun observeFavorites(query: String): Flow<List<FavoriteMovie>> = store.map { map ->
        map.values
            .filter { fav ->
                query.isBlank() ||
                    fav.movie.title.contains(query, ignoreCase = true) ||
                    fav.note?.contains(query, ignoreCase = true) == true
            }
            .sortedByDescending { it.addedAt }
    }

    override fun observeFavoriteIds(): Flow<Set<Int>> = store.map { it.keys }

    override fun observeFavorite(movieId: Int): Flow<FavoriteMovie?> = store.map { it[movieId] }

    override suspend fun getFavorite(movieId: Int): FavoriteMovie? = store.value[movieId]

    override suspend fun save(favorite: FavoriteMovie) = store.update { it + (favorite.id to favorite) }

    override suspend fun remove(movieId: Int) = store.update { it - movieId }

    override suspend fun updateNote(movieId: Int, note: String?, updatedAt: Instant?) = store.update { map ->
        val existing = map[movieId] ?: return@update map
        map + (movieId to existing.copy(note = note, noteUpdatedAt = updatedAt))
    }
}
