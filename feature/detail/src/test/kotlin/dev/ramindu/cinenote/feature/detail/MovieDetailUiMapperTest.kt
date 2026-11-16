package dev.ramindu.cinenote.feature.detail

import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.testing.TestData
import org.junit.jupiter.api.Test

class MovieDetailUiMapperTest {

    @Test
    fun `maps detail into display-ready fields`() {
        val ui = TestData.movieDetail(movie = TestData.movie(id = 1, rating = 8.44), runtimeMinutes = 169).toUi()

        assertThat(ui.ratingLabel).isEqualTo("8.4")
        assertThat(ui.runtimeLabel).isEqualTo("2h 49m")
        assertThat(ui.yearLabel).isEqualTo("2024")
        assertThat(ui.cast.single().name).isEqualTo("Actor One")
        assertThat(ui.similar.single().id).isEqualTo(99)
        assertThat(ui.trailerUrl).isEqualTo("https://www.youtube.com/watch?v=abc123")
    }

    @Test
    fun `falls back to the poster when there is no backdrop`() {
        val movie = TestData.movie(backdropUrl = null, posterUrl = "poster")

        assertThat(TestData.movieDetail(movie = movie).toUi().backdropUrl).isEqualTo("poster")
    }

    @Test
    fun `missing trailer and runtime are null`() {
        val ui = TestData.movieDetail(trailer = null, runtimeMinutes = null).toUi()

        assertThat(ui.trailerUrl).isNull()
        assertThat(ui.runtimeLabel).isNull()
    }
}
