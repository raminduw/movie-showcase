package dev.ramindu.cinenote.core.data.mapper

import dev.ramindu.cinenote.core.domain.model.CastMember
import dev.ramindu.cinenote.core.domain.model.Genre
import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.model.MovieDetail
import dev.ramindu.cinenote.core.domain.model.Trailer
import dev.ramindu.cinenote.core.network.dto.CastDto
import dev.ramindu.cinenote.core.network.dto.MovieDetailDto
import dev.ramindu.cinenote.core.network.dto.MovieDto
import dev.ramindu.cinenote.core.network.dto.VideoDto
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** DTO → domain. All "TMDB weirdness" (relative paths, empty dates, nulls) is absorbed here. */

private const val MAX_CAST = 15
private const val MAX_SIMILAR = 20

fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title.orEmpty().ifBlank { "Untitled" },
    overview = overview.orEmpty(),
    posterUrl = TmdbImage.poster(posterPath),
    backdropUrl = TmdbImage.backdrop(backdropPath),
    releaseDate = parseReleaseDate(releaseDate),
    rating = voteAverage ?: 0.0,
    voteCount = voteCount ?: 0,
)

fun MovieDetailDto.toDomain(): MovieDetail = MovieDetail(
    movie = Movie(
        id = id,
        title = title.orEmpty().ifBlank { "Untitled" },
        overview = overview.orEmpty(),
        posterUrl = TmdbImage.poster(posterPath),
        backdropUrl = TmdbImage.backdrop(backdropPath),
        releaseDate = parseReleaseDate(releaseDate),
        rating = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
    ),
    tagline = tagline?.takeIf { it.isNotBlank() },
    runtimeMinutes = runtime?.takeIf { it > 0 },
    genres = genres.map { Genre(it.id, it.name) },
    cast = credits?.cast.orEmpty()
        .sortedBy { it.order ?: Int.MAX_VALUE }
        .take(MAX_CAST)
        .map { it.toDomain() },
    similar = similar?.results.orEmpty()
        .filter { it.id != id }
        .take(MAX_SIMILAR)
        .map { it.toDomain() },
    trailer = videos?.results.orEmpty().pickTrailer(),
)

fun CastDto.toDomain(): CastMember = CastMember(
    id = id,
    name = name.orEmpty(),
    character = character?.takeIf { it.isNotBlank() },
    profileUrl = TmdbImage.profile(profilePath),
)

/** Prefer an official YouTube "Trailer", then any YouTube trailer, then any YouTube teaser. */
internal fun List<VideoDto>.pickTrailer(): Trailer? {
    val youtube = filter { it.site.equals("YouTube", ignoreCase = true) && it.key.isNotBlank() }
    val best = youtube.firstOrNull { it.type == "Trailer" && it.official == true }
        ?: youtube.firstOrNull { it.type == "Trailer" }
        ?: youtube.firstOrNull { it.type == "Teaser" }
    return best?.let { Trailer(name = it.name ?: "Trailer", youtubeKey = it.key) }
}

/** TMDB sends "yyyy-MM-dd", "" or null. */
internal fun parseReleaseDate(raw: String?): LocalDate? =
    raw?.takeIf { it.isNotBlank() }?.let {
        try {
            LocalDate.parse(it)
        } catch (_: DateTimeParseException) {
            null
        }
    }
