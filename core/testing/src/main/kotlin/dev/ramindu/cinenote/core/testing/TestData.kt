package dev.ramindu.cinenote.core.testing

import dev.ramindu.cinenote.core.domain.model.CastMember
import dev.ramindu.cinenote.core.domain.model.FavoriteMovie
import dev.ramindu.cinenote.core.domain.model.Genre
import dev.ramindu.cinenote.core.domain.model.Movie
import dev.ramindu.cinenote.core.domain.model.MovieDetail
import dev.ramindu.cinenote.core.domain.model.MovieSnapshot
import dev.ramindu.cinenote.core.domain.model.Trailer
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Builders with sensible defaults – tests override only what they care about. */
object TestData {
    val NOW: Instant = Instant.parse("2026-01-15T10:00:00Z")
    val fixedClock: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

    fun movie(
        id: Int = 1,
        title: String = "Movie $id",
        overview: String = "Overview $id",
        posterUrl: String? = "https://image.tmdb.org/t/p/w500/poster$id.jpg",
        backdropUrl: String? = "https://image.tmdb.org/t/p/w1280/backdrop$id.jpg",
        releaseDate: LocalDate? = LocalDate.of(2024, 5, 17),
        rating: Double = 7.5,
        voteCount: Int = 1000,
    ) = Movie(id, title, overview, posterUrl, backdropUrl, releaseDate, rating, voteCount)

    fun snapshot(
        id: Int = 1,
        title: String = "Movie $id",
        posterUrl: String? = "https://image.tmdb.org/t/p/w500/poster$id.jpg",
        releaseYear: Int? = 2024,
        rating: Double = 7.5,
    ) = MovieSnapshot(id, title, posterUrl, releaseYear, rating)

    fun favorite(
        id: Int = 1,
        title: String = "Movie $id",
        note: String? = null,
        addedAt: Instant = NOW,
        noteUpdatedAt: Instant? = null,
    ) = FavoriteMovie(snapshot(id = id, title = title), note, addedAt, noteUpdatedAt)

    fun movieDetail(
        movie: Movie = movie(),
        tagline: String? = "A tagline",
        runtimeMinutes: Int? = 125,
        genres: List<Genre> = listOf(Genre(28, "Action"), Genre(12, "Adventure")),
        cast: List<CastMember> = listOf(CastMember(10, "Actor One", "Hero", null)),
        similar: List<Movie> = listOf(movie(id = 99)),
        trailer: Trailer? = Trailer("Official Trailer", "abc123"),
    ) = MovieDetail(movie, tagline, runtimeMinutes, genres, cast, similar, trailer)
}
