package dev.ramindu.cinenote.core.data.error

import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppException
import dev.ramindu.cinenote.core.domain.result.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErrorMapperTest {

    @ParameterizedTest(name = "HTTP {0} -> {1}")
    @CsvSource("401,UNAUTHORIZED", "403,UNAUTHORIZED", "404,NOT_FOUND", "500,SERVER", "503,SERVER", "429,UNKNOWN")
    fun `maps http status codes`(code: Int, expected: AppError) {
        assertThat(httpException(code).toAppError()).isEqualTo(expected)
    }

    @Test
    fun `io problems mean no internet`() {
        assertThat(IOException("timeout").toAppError()).isEqualTo(AppError.NO_INTERNET)
    }

    @Test
    fun `bad json is unknown`() {
        assertThat(SerializationException("bad").toAppError()).isEqualTo(AppError.UNKNOWN)
    }

    @Test
    fun `app exception keeps its error`() {
        assertThat(AppException(AppError.SERVER).toAppError()).isEqualTo(AppError.SERVER)
    }

    @Test
    fun `safeCall wraps success`() = runTest {
        assertThat(safeCall { 42 }).isEqualTo(AppResult.Success(42))
    }

    @Test
    fun `safeCall wraps failure`() = runTest {
        assertThat(safeCall { throw IOException() }).isEqualTo(AppResult.Failure(AppError.NO_INTERNET))
    }

    @Test
    fun `safeCall rethrows cancellation`() = runTest {
        assertThrows<CancellationException> { safeCall { throw CancellationException("cancelled") } }
    }

    private fun httpException(code: Int) =
        HttpException(Response.error<Any>(code, "{}".toResponseBody()))
}
