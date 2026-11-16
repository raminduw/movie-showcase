package dev.ramindu.cinenote.feature.detail

import dev.ramindu.cinenote.core.domain.model.CastMember
import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.model.MovieDetail
import dev.ramindu.cinenote.core.ui.Formatters

data class MovieDetailUi(
    val id: Int,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val yearLabel: String,
    val ratingLabel: String,
    val voteCount: Int,
    val runtimeLabel: String?,
    val genres: List<String>,
    val cast: List<CastUi>,
    val similar: List<SimilarMovieUi>,
    val trailerUrl: String?,
)

data class CastUi(val id: Int, val name: String, val character: String?, val profileUrl: String?)

data class SimilarMovieUi(val id: Int, val title: String, val posterUrl: String?, val ratingLabel: String)

fun MovieDetail.toUi(): MovieDetailUi = MovieDetailUi(
    id = movie.id,
    title = movie.title,
    tagline = tagline,
    overview = movie.overview,
    posterUrl = movie.posterUrl,
    backdropUrl = movie.backdropUrl ?: movie.posterUrl,
    yearLabel = Formatters.year(movie.releaseYear),
    ratingLabel = Formatters.rating(movie.rating),
    voteCount = movie.voteCount,
    runtimeLabel = Formatters.runtime(runtimeMinutes),
    genres = genres.map { it.name },
    cast = cast.map { it.toUi() },
    similar = similar.map { it.toSimilarUi() },
    trailerUrl = trailer?.url,
)

private fun CastMember.toUi() = CastUi(id, name, character, profileUrl)

private fun Movie.toSimilarUi() = SimilarMovieUi(id, title, posterUrl, Formatters.rating(rating))
