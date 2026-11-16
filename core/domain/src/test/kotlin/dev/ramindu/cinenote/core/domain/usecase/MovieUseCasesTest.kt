package dev.ramindu.cinenote.core.domain.usecase

import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.domain.model.MovieCategory
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppResult
import dev.ramindu.cinenote.core.testing.FakeMovieRepository
import dev.ramindu.cinenote.core.testing.TestData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class MovieUseCasesTest {

    private val repository = FakeMovieRepository()

    @Nested
    inner class GetMoviesByCategory {
        private val useCase = GetMoviesByCategoryUseCase(repository)

        @ParameterizedTest
        @EnumSource(MovieCategory::class)
        fun `requests the given category from the repository`(category: MovieCategory) = runTest {
            repository.moviesByCategory = mapOf(category to listOf(TestData.movie(id = 7)))

            val movies = useCase(category).asSnapshot()

            assertThat(repository.requestedCategories).containsExactly(category)
            assertThat(movies.map { it.id }).containsExactly(7)
        }
    }

    @Nested
    inner class SearchMovies {
        private val useCase = SearchMoviesUseCase(repository)

        @Test
        fun `trims the query before searching`() = runTest {
            repository.searchResults = listOf(TestData.movie(id = 3))

            val movies = useCase("  dune  ").asSnapshot()

            assertThat(repository.searchQueries).containsExactly("dune")
            assertThat(movies.map { it.id }).containsExactly(3)
        }

        @ParameterizedTest
        @ValueSource(strings = ["", " ", "\t\n"])
        fun `blank query returns empty results without calling the repository`(query: String) = runTest {
            val movies = useCase(query).asSnapshot()

            assertThat(movies).isEmpty()
            assertThat(repository.searchQueries).isEmpty()
        }
    }

    @Nested
    inner class GetMovieDetail {
        private val useCase = GetMovieDetailUseCase(repository)

        @Test
        fun `returns the repository result for a valid id`() = runTest {
            val detail = TestData.movieDetail()
            repository.detailResult = AppResult.Success(detail)

            val result = useCase(1)

            assertThat(result).isEqualTo(AppResult.Success(detail))
            assertThat(repository.detailRequests).containsExactly(1)
        }

        @ParameterizedTest
        @ValueSource(ints = [0, -1])
        fun `invalid id fails fast with NOT_FOUND`(id: Int) = runTest {
            val result = useCase(id)

            assertThat(result).isEqualTo(AppResult.Failure(AppError.NOT_FOUND))
            assertThat(repository.detailRequests).isEmpty()
        }
    }
}
