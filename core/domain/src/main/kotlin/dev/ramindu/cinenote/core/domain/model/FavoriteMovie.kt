package dev.ramindu.cinenote.core.domain.model

import java.time.Instant

/** A movie the user saved, plus their personal note about it. */
data class FavoriteMovie(
    val movie: MovieSnapshot,
    val note: String?,
    val addedAt: Instant,
    val noteUpdatedAt: Instant?,
) {
    val id: Int get() = movie.id
    val hasNote: Boolean get() = !note.isNullOrBlank()
}
