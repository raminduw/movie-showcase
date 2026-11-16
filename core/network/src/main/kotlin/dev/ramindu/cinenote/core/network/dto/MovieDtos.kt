package dev.ramindu.cinenote.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Raw TMDB JSON shapes. Nullable/defaulted because the API omits fields freely. */

@Serializable
data class PagedResponseDto<T>(
    @SerialName("page") val page: Int,
    @SerialName("results") val results: List<T> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class MovieDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int? = null,
)

@Serializable
data class MovieDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("tagline") val tagline: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
    @SerialName("credits") val credits: CreditsDto? = null,
    @SerialName("videos") val videos: VideosDto? = null,
    @SerialName("similar") val similar: PagedResponseDto<MovieDto>? = null,
)

@Serializable
data class GenreDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
)

@Serializable
data class CreditsDto(
    @SerialName("cast") val cast: List<CastDto> = emptyList(),
)

@Serializable
data class CastDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("character") val character: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("order") val order: Int? = null,
)

@Serializable
data class VideosDto(
    @SerialName("results") val results: List<VideoDto> = emptyList(),
)

@Serializable
data class VideoDto(
    @SerialName("key") val key: String,
    @SerialName("name") val name: String? = null,
    @SerialName("site") val site: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("official") val official: Boolean? = null,
)
