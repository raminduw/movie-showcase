package dev.ramindu.cinenote.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.domain.model.MovieShare
import dev.ramindu.cinenote.core.testing.TestData
import org.junit.jupiter.api.Test

class CreateMovieShareUseCaseTest {

    private val useCase = CreateMovieShareUseCase()

    @Test
    fun `builds the canonical TMDB link and keeps the details`() {
        val share = useCase(TestData.snapshot(id = 550, title = "Fight Club", releaseYear = 1999, rating = 8.4))

        assertThat(share).isEqualTo(
            MovieShare(title = "Fight Club", releaseYear = 1999, rating = 8.4, url = "https://www.themoviedb.org/movie/550"),
        )
    }

    @Test
    fun `trims the title and tolerates a missing year`() {
        val share = useCase(TestData.snapshot(id = 1, title = "  Untitled Project ", releaseYear = null))

        assertThat(share.title).isEqualTo("Untitled Project")
        assertThat(share.releaseYear).isNull()
    }
}
