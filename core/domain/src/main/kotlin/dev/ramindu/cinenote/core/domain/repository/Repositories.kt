package dev.ramindu.cinenote.core.domain.repository

import androidx.paging.PagingData
import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.model.MovieCategory
import dev.ramindu.cinenote.core.domain.model.MovieDetail
import dev.ramindu.cinenote.core.domain.result.AppResult
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Contracts live in the domain; implementations live in :core:data (Dependency Inversion).
 * Use cases depend only on the narrow interface they need (Interface Segregation).
 */
interface MovieRepository {
    fun moviesByCategory(category: MovieCategory): Flow<PagingData<Movie>>
    fun searchMovies(query: String): Flow<PagingData<Movie>>
    suspend fun movieDetail(movieId: Int): AppResult<MovieDetail>
}

interface FavoritesReader {
    /** Favourites newest first. A non-blank [query] matches the title or the note. */
    fun observeFavorites(query: String): Flow<List<FavoriteMovie>>
    fun observeFavoriteIds(): Flow<Set<Int>>
    fun observeFavorite(movieId: Int): Flow<FavoriteMovie?>
    suspend fun getFavorite(movieId: Int): FavoriteMovie?
}

interface FavoritesWriter {
    suspend fun save(favorite: FavoriteMovie)
    suspend fun remove(movieId: Int)
    suspend fun updateNote(movieId: Int, note: String?, updatedAt: Instant?)
}

interface FavoritesRepository : FavoritesReader, FavoritesWriter
