package dev.ramindu.cinenote.core.network

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Test

class AuthInterceptorTest {

    private val original = Request.Builder()
        .url("https://api.themoviedb.org/3/movie/popular?page=1")
        .header("X-Custom", "keep")
        .build()

    private fun intercept(token: String): Request {
        val captured = slot<Request>()
        val chain = mockk<Interceptor.Chain> {
            every { request() } returns original
            every { proceed(capture(captured)) } answers {
                Response.Builder().request(captured.captured).protocol(Protocol.HTTP_1_1).code(200).message("OK").build()
            }
        }
        AuthInterceptor(token).intercept(chain)
        return captured.captured
    }

    @Test
    fun `read access token (JWT) is sent as a bearer header`() {
        val request = intercept("eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJ4In0.signature")

        assertThat(request.header("Authorization")).isEqualTo("Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJ4In0.signature")
        assertThat(request.url.queryParameter("api_key")).isNull()
    }

    @Test
    fun `v3 api key is sent as a query parameter`() {
        val request = intercept("0123456789abcdef0123456789abcdef")

        assertThat(request.url.queryParameter("api_key")).isEqualTo("0123456789abcdef0123456789abcdef")
        assertThat(request.url.queryParameter("page")).isEqualTo("1")
        assertThat(request.header("Authorization")).isNull()
    }

    @Test
    fun `surrounding whitespace from local properties is ignored`() {
        val request = intercept("  eyJa.eyJb.sig \n")

        assertThat(request.header("Authorization")).isEqualTo("Bearer eyJa.eyJb.sig")
    }

    @Test
    fun `blank token sends no credential, so TMDB answers 401 and the UI explains it`() {
        val request = intercept("")

        assertThat(request.header("Authorization")).isNull()
        assertThat(request.url.queryParameter("api_key")).isNull()
    }

    @Test
    fun `keeps existing headers and adds accept json`() {
        val request = intercept("eyJa.eyJb.sig")

        assertThat(request.header("X-Custom")).isEqualTo("keep")
        assertThat(request.header("Accept")).isEqualTo("application/json")
        assertThat(request.url.encodedPath).isEqualTo(original.url.encodedPath)
    }
}
