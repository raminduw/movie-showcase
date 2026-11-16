package dev.ramindu.cinenote.core.domain.model

import java.time.LocalDate

/** A movie as the app understands it – independent of TMDB's JSON shape. */
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: LocalDate?,
    val rating: Double,
    val voteCount: Int,
) {
    val releaseYear: Int? get() = releaseDate?.year
}

/** The minimum we need to remember about a movie when it is saved locally. */
data class MovieSnapshot(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val releaseYear: Int?,
    val rating: Double,
)

fun Movie.toSnapshot(): MovieSnapshot = MovieSnapshot(
    id = id,
    title = title,
    posterUrl = posterUrl,
    releaseYear = releaseYear,
    rating = rating,
)

enum class MovieCategory {
    POPULAR,
    TRENDING,
    TOP_RATED,
    NOW_PLAYING,
    UPCOMING,
}
