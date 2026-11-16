package dev.ramindu.cinenote.core.network

import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.network.di.createTmdbApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException

/** Verifies request shape (paths, query params, headers) and JSON parsing against a local server. */
class TmdbApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TmdbApi

    @BeforeEach
    fun setUp() {
        server = MockWebServer().apply { start() }
        val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor("test.read.token")).build()
        api = createTmdbApi(baseUrl = server.url("/3/").toString(), client = client)
    }

    @AfterEach
    fun tearDown() = server.shutdown()

    @Test
    fun `movieList requests the list path with page and bearer token`() = runTest {
        server.enqueue(jsonResponse("popular_movies.json"))

        api.movieList(list = "popular", page = 2)

        val request = server.takeRequest()
        assertThat(request.requestUrl!!.encodedPath).isEqualTo("/3/movie/popular")
        assertThat(request.requestUrl!!.queryParameter("page")).isEqualTo("2")
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test.read.token")
    }

    @Test
    fun `parses a paged movie response including null and empty fields`() = runTest {
        server.enqueue(jsonResponse("popular_movies.json"))

        val response = api.movieList(list = "popular", page = 1)

        assertThat(response.page).isEqualTo(1)
        assertThat(response.totalPages).isEqualTo(500)
        assertThat(response.results).hasSize(2)
        with(response.results[0]) {
            assertThat(id).isEqualTo(1022789)
            assertThat(title).isEqualTo("Inside Out 2")
            assertThat(posterPath).isEqualTo("/poster1.jpg")
            assertThat(releaseDate).isEqualTo("2024-06-11")
            assertThat(voteAverage).isEqualTo(7.6)
        }
        assertThat(response.results[1].posterPath).isNull()
    }

    @Test
    fun `search sends the query and excludes adult content`() = runTest {
        server.enqueue(jsonResponse("popular_movies.json"))

        api.searchMovies(query = "inside out", page = 1)

        val url = server.takeRequest().requestUrl!!
        assertThat(url.encodedPath).isEqualTo("/3/search/movie")
        assertThat(url.queryParameter("query")).isEqualTo("inside out")
        assertThat(url.queryParameter("include_adult")).isEqualTo("false")
    }

    @Test
    fun `trending uses the weekly trending endpoint`() = runTest {
        server.enqueue(jsonResponse("popular_movies.json"))

        api.trendingMovies(page = 3)

        val url = server.takeRequest().requestUrl!!
        assertThat(url.encodedPath).isEqualTo("/3/trending/movie/week")
        assertThat(url.queryParameter("page")).isEqualTo("3")
    }

    @Test
    fun `detail appends credits videos and similar and parses them`() = runTest {
        server.enqueue(jsonResponse("movie_detail.json"))

        val detail = api.movieDetail(1022789)

        val url = server.takeRequest().requestUrl!!
        assertThat(url.encodedPath).isEqualTo("/3/movie/1022789")
        assertThat(url.queryParameter("append_to_response")).isEqualTo("credits,videos,similar")
        assertThat(detail.runtime).isEqualTo(97)
        assertThat(detail.genres.map { it.name }).containsExactly("Animation", "Family").inOrder()
        assertThat(detail.credits!!.cast).hasSize(2)
        assertThat(detail.videos!!.results.map { it.key }).containsExactly("teaser1", "trailer1")
        assertThat(detail.similar!!.results.single().id).isEqualTo(150540)
    }

    @Test
    fun `non-2xx response surfaces as HttpException with the status code`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"status_code":7}"""))

        val error = assertThrows<HttpException> { api.movieList("popular", 1) }

        assertThat(error.code()).isEqualTo(401)
    }

    private fun jsonResponse(fileName: String): MockResponse {
        val body = requireNotNull(javaClass.classLoader?.getResource(fileName)) { "Missing fixture $fileName" }.readText()
        return MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(body)
    }
}
