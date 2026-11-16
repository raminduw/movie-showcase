package dev.ramindu.cinenote.feature.movies

import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.testing.TestData
import org.junit.jupiter.api.Test

class MovieUiMapperTest {

    @Test
    fun `formats rating and year for display`() {
        val ui = TestData.movie(id = 1, rating = 8.26, releaseDate = null).toUi()

        assertThat(ui.ratingLabel).isEqualTo("8.3")
        assertThat(ui.yearLabel).isEqualTo("TBA")
    }

    @Test
    fun `snapshot keeps the data needed to save a favourite`() {
        val movie = TestData.movie(id = 9, title = "Up", rating = 8.0)

        val snapshot = movie.toUi().toSnapshot()

        assertThat(snapshot).isEqualTo(TestData.snapshot(id = 9, title = "Up", posterUrl = movie.posterUrl, releaseYear = 2024, rating = 8.0))
    }
}
