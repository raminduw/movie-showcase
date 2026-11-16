package dev.ramindu.cinenote.core.data.mapper

import dev.ramindu.cinenote.core.database.FavoriteEntity
import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.domain.model.MovieSnapshot
import java.time.Instant

/** Room entity ↔ domain. Timestamps are stored as epoch millis. */

fun FavoriteEntity.toDomain(): FavoriteMovie = FavoriteMovie(
    movie = MovieSnapshot(
        id = movieId,
        title = title,
        posterUrl = posterUrl,
        releaseYear = releaseYear,
        rating = rating,
    ),
    note = note,
    addedAt = Instant.ofEpochMilli(addedAtEpochMillis),
    noteUpdatedAt = noteUpdatedAtEpochMillis?.let(Instant::ofEpochMilli),
)

fun FavoriteMovie.toEntity(): FavoriteEntity = FavoriteEntity(
    movieId = movie.id,
    title = movie.title,
    posterUrl = movie.posterUrl,
    releaseYear = movie.releaseYear,
    rating = movie.rating,
    addedAtEpochMillis = addedAt.toEpochMilli(),
    note = note,
    noteUpdatedAtEpochMillis = noteUpdatedAt?.toEpochMilli(),
)
