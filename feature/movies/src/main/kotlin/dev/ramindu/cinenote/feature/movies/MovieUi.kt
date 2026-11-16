package dev.ramindu.cinenote.feature.movies

import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.model.MovieSnapshot
import dev.ramindu.cinenote.core.ui.Formatters

/** What a list row needs – already formatted, so the composable only lays it out. */
data class MovieUi(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val releaseYear: Int?,
    val yearLabel: String,
    val rating: Double,
    val ratingLabel: String,
)

/** Domain → presentation. */
fun Movie.toUi(): MovieUi = MovieUi(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    releaseYear = releaseYear,
    yearLabel = Formatters.year(releaseYear),
    rating = rating,
    ratingLabel = Formatters.rating(rating),
)

/** Presentation → domain, for saving a favourite from the list. */
fun MovieUi.toSnapshot(): MovieSnapshot = MovieSnapshot(
    id = id,
    title = title,
    posterUrl = posterUrl,
    releaseYear = releaseYear,
    rating = rating,
)
