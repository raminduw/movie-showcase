package dev.ramindu.cinenote.core.data.mapper

import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.network.dto.CastDto
import dev.ramindu.cinenote.core.network.dto.CreditsDto
import dev.ramindu.cinenote.core.network.dto.GenreDto
import dev.ramindu.cinenote.core.network.dto.MovieDetailDto
import dev.ramindu.cinenote.core.network.dto.MovieDto
import dev.ramindu.cinenote.core.network.dto.PagedResponseDto
import dev.ramindu.cinenote.core.network.dto.VideoDto
import dev.ramindu.cinenote.core.network.dto.VideosDto
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate

class MovieMappersTest {

    @Nested
    inner class MovieDtoToDomain {

        @Test
        fun `maps all fields and builds full image urls`() {
            val dto = MovieDto(
                id = 1,
                title = "Dune",
                overview = "Spice",
                posterPath = "/p.jpg",
                backdropPath = "/b.jpg",
                releaseDate = "2021-10-22",
                voteAverage = 7.8,
                voteCount = 9000,
            )

            val movie = dto.toDomain()

            assertThat(movie.id).isEqualTo(1)
            assertThat(movie.title).isEqualTo("Dune")
            assertThat(movie.overview).isEqualTo("Spice")
            assertThat(movie.posterUrl).isEqualTo("https://image.tmdb.org/t/p/w500/p.jpg")
            assertThat(movie.backdropUrl).isEqualTo("https://image.tmdb.org/t/p/w1280/b.jpg")
            assertThat(movie.releaseDate).isEqualTo(LocalDate.of(2021, 10, 22))
            assertThat(movie.releaseYear).isEqualTo(2021)
            assertThat(movie.rating).isEqualTo(7.8)
            assertThat(movie.voteCount).isEqualTo(9000)
        }

        @Test
        fun `missing values fall back to safe defaults`() {
            val movie = MovieDto(id = 2).toDomain()

            assertThat(movie.title).isEqualTo("Untitled")
            assertThat(movie.overview).isEmpty()
            assertThat(movie.posterUrl).isNull()
            assertThat(movie.backdropUrl).isNull()
            assertThat(movie.releaseDate).isNull()
            assertThat(movie.rating).isEqualTo(0.0)
            assertThat(movie.voteCount).isEqualTo(0)
        }
    }

    @Nested
    inner class ReleaseDate {
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = ["", "  ", "not-a-date", "2024-13-01"])
        fun `invalid or empty dates become null`(raw: String?) {
            assertThat(parseReleaseDate(raw)).isNull()
        }
    }

    @Nested
    inner class DetailToDomain {

        private val dto = MovieDetailDto(
            id = 10,
            title = "Heat",
            tagline = "",
            runtime = 0,
            genres = listOf(GenreDto(80, "Crime")),
            credits = CreditsDto(
                cast = listOf(
                    CastDto(id = 2, name = "Second", character = "", order = 1),
                    CastDto(id = 1, name = "First", character = "Lead", profilePath = "/a.jpg", order = 0),
                ),
            ),
            videos = VideosDto(listOf(VideoDto(key = "k", name = "Trailer", site = "YouTube", type = "Trailer", official = true))),
            similar = PagedResponseDto(page = 1, results = listOf(MovieDto(id = 10), MovieDto(id = 11))),
        )

        @Test
        fun `orders cast by billing order and maps profile urls`() {
            val cast = dto.toDomain().cast

            assertThat(cast.map { it.name }).containsExactly("First", "Second").inOrder()
            assertThat(cast[0].profileUrl).isEqualTo("https://image.tmdb.org/t/p/w185/a.jpg")
            assertThat(cast[1].character).isNull()
        }

        @Test
        fun `blank tagline and zero runtime become null`() {
            val detail = dto.toDomain()

            assertThat(detail.tagline).isNull()
            assertThat(detail.runtimeMinutes).isNull()
        }

        @Test
        fun `similar list never contains the movie itself`() {
            assertThat(dto.toDomain().similar.map { it.id }).containsExactly(11)
        }

        @Test
        fun `maps genres and trailer`() {
            val detail = dto.toDomain()

            assertThat(detail.genres.single().name).isEqualTo("Crime")
            assertThat(detail.trailer?.url).isEqualTo("https://www.youtube.com/watch?v=k")
        }
    }

    @Nested
    inner class PickTrailer {
        @Test
        fun `prefers official youtube trailer over teaser and other sites`() {
            val videos = listOf(
                VideoDto(key = "vimeo", site = "Vimeo", type = "Trailer", official = true),
                VideoDto(key = "teaser", site = "YouTube", type = "Teaser", official = true),
                VideoDto(key = "fan", site = "YouTube", type = "Trailer", official = false),
                VideoDto(key = "official", site = "YouTube", type = "Trailer", official = true),
            )

            assertThat(videos.pickTrailer()?.youtubeKey).isEqualTo("official")
        }

        @Test
        fun `falls back to a teaser when there is no trailer`() {
            val videos = listOf(VideoDto(key = "teaser", site = "YouTube", type = "Teaser"))

            assertThat(videos.pickTrailer()?.youtubeKey).isEqualTo("teaser")
        }

        @Test
        fun `returns null when nothing playable exists`() {
            val videos = listOf(VideoDto(key = "bts", site = "YouTube", type = "Behind the Scenes"))

            assertThat(videos.pickTrailer()).isNull()
        }
    }

    @Nested
    inner class Images {
        @Test
        fun `blank path returns null and path without slash is normalised`() {
            assertThat(TmdbImage.poster("")).isNull()
            assertThat(TmdbImage.poster(null)).isNull()
            assertThat(TmdbImage.poster("x.jpg")).isEqualTo("https://image.tmdb.org/t/p/w500/x.jpg")
        }
    }
}
