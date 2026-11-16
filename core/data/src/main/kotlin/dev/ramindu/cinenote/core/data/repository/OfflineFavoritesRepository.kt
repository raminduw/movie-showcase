package dev.ramindu.cinenote.core.data.repository

import dev.ramindu.cinenote.core.data.mapper.toDomain
import dev.ramindu.cinenote.core.data.mapper.toEntity
import dev.ramindu.cinenote.core.database.FavoriteDao
import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/** Favourites + notes live only on the device (Room). */
class OfflineFavoritesRepository @Inject constructor(
    private val dao: FavoriteDao,
) : FavoritesRepository {

    override fun observeFavorites(query: String): Flow<List<FavoriteMovie>> =
        dao.observeFavorites(query.trim()).map { entities -> entities.map { it.toDomain() } }

    override fun observeFavoriteIds(): Flow<Set<Int>> =
        dao.observeIds().map { it.toSet() }.distinctUntilChanged()

    override fun observeFavorite(movieId: Int): Flow<FavoriteMovie?> =
        dao.observeById(movieId).map { it?.toDomain() }

    override suspend fun getFavorite(movieId: Int): FavoriteMovie? = dao.getById(movieId)?.toDomain()

    override suspend fun save(favorite: FavoriteMovie) = dao.upsert(favorite.toEntity())

    override suspend fun remove(movieId: Int) = dao.deleteById(movieId)

    override suspend fun updateNote(movieId: Int, note: String?, updatedAt: Instant?) {
        dao.updateNote(movieId, note, updatedAt?.toEpochMilli())
    }
}
