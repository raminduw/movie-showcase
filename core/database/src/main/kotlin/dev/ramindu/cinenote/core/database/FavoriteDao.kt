package dev.ramindu.cinenote.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    /** Newest first. Empty [query] returns everything; otherwise matches title OR note (case-insensitive). */
    @Query(
        """
        SELECT * FROM favorites
        WHERE :query = '' OR title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%'
        ORDER BY added_at DESC
        """,
    )
    fun observeFavorites(query: String): Flow<List<FavoriteEntity>>

    @Query("SELECT movie_id FROM favorites")
    fun observeIds(): Flow<List<Int>>

    @Query("SELECT * FROM favorites WHERE movie_id = :movieId")
    fun observeById(movieId: Int): Flow<FavoriteEntity?>

    @Query("SELECT * FROM favorites WHERE movie_id = :movieId")
    suspend fun getById(movieId: Int): FavoriteEntity?

    @Upsert
    suspend fun upsert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE movie_id = :movieId")
    suspend fun deleteById(movieId: Int)

    /** @return number of rows updated (0 when the movie is not a favourite). */
    @Query("UPDATE favorites SET note = :note, note_updated_at = :updatedAtEpochMillis WHERE movie_id = :movieId")
    suspend fun updateNote(movieId: Int, note: String?, updatedAtEpochMillis: Long?): Int
}
