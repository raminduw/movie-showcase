package dev.ramindu.cinenote.core.domain.model

data class MovieDetail(
    val movie: Movie,
    val tagline: String?,
    val runtimeMinutes: Int?,
    val genres: List<Genre>,
    val cast: List<CastMember>,
    val similar: List<Movie>,
    val trailer: Trailer?,
)

data class Genre(val id: Int, val name: String)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String?,
    val profileUrl: String?,
)

data class Trailer(val name: String, val youtubeKey: String) {
    val url: String get() = "https://www.youtube.com/watch?v=$youtubeKey"
}
