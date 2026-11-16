package dev.ramindu.cinenote.core.data.repository

import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.domain.model.MovieCategory
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppResult
import dev.ramindu.cinenote.core.network.TmdbApi
import dev.ramindu.cinenote.core.network.dto.MovieDetailDto
import dev.ramindu.cinenote.core.network.dto.MovieDto
import dev.ramindu.cinenote.core.network.dto.PagedResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.IOException

class TmdbMovieRepositoryTest {

    private val api = mockk<TmdbApi>()
    private val repository = TmdbMovieRepository(api)

    private fun page(vararg ids: Int) =
        PagedResponseDto(page = 1, results = ids.map { MovieDto(id = it, title = "M$it") }, totalPages = 1)

    @ParameterizedTest
    @CsvSource("POPULAR,popular", "TOP_RATED,top_rated", "NOW_PLAYING,now_playing", "UPCOMING,upcoming")
    fun `list categories call the matching movie list path`(category: MovieCategory, path: String) = runTest {
        coEvery { api.movieList(path, 1) } returns page(1, 2)

        val movies = repository.moviesByCategory(category).asSnapshot()

        assertThat(movies.map { it.id }).containsExactly(1, 2).inOrder()
        coVerify { api.movieList(path, 1) }
    }

    @Test
    fun `trending uses the trending endpoint`() = runTest {
        coEvery { api.trendingMovies(1) } returns page(5)

        val movies = repository.moviesByCategory(MovieCategory.TRENDING).asSnapshot()

        assertThat(movies.map { it.id }).containsExactly(5)
    }

    @Test
    fun `search passes the query through and maps results`() = runTest {
        coEvery { api.searchMovies("matrix", 1, false) } returns page(603)

        val movies = repository.searchMovies("matrix").asSnapshot()

        assertThat(movies.single().title).isEqualTo("M603")
    }

    @Test
    fun `detail success is mapped to the domain`() = runTest {
        coEvery { api.movieDetail(7, any()) } returns MovieDetailDto(id = 7, title = "Se7en", runtime = 127)

        val result = repository.movieDetail(7)

        val detail = (result as AppResult.Success).data
        assertThat(detail.movie.title).isEqualTo("Se7en")
        assertThat(detail.runtimeMinutes).isEqualTo(127)
    }

    @Test
    fun `detail failure is mapped to an AppError`() = runTest {
        coEvery { api.movieDetail(7, any()) } throws IOException()

        assertThat(repository.movieDetail(7)).isEqualTo(AppResult.Failure(AppError.NO_INTERNET))
    }
}
