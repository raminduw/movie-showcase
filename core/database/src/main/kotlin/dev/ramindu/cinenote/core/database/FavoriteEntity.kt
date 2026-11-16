package dev.ramindu.cinenote.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "movie_id") val movieId: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "poster_url") val posterUrl: String?,
    @ColumnInfo(name = "release_year") val releaseYear: Int?,
    @ColumnInfo(name = "rating") val rating: Double,
    @ColumnInfo(name = "added_at") val addedAtEpochMillis: Long,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "note_updated_at") val noteUpdatedAtEpochMillis: Long?,
)
