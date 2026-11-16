package dev.ramindu.cinenote.core.data.error

import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppException
import dev.ramindu.cinenote.core.domain.result.AppResult
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Translates transport-level exceptions into domain errors. */
fun Throwable.toAppError(): AppError = when (this) {
    is AppException -> error
    is IOException -> AppError.NO_INTERNET
    is HttpException -> when (code()) {
        401, 403 -> AppError.UNAUTHORIZED
        404 -> AppError.NOT_FOUND
        in 500..599 -> AppError.SERVER
        else -> AppError.UNKNOWN
    }
    is SerializationException -> AppError.UNKNOWN
    else -> AppError.UNKNOWN
}

/** Runs [block] and wraps the outcome. Cancellation is re-thrown so structured concurrency still works. */
suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    AppResult.Failure(e.toAppError())
}
