package dev.ramindu.cinenote.core.domain.result

/** Failure reasons the UI knows how to explain. Data-layer exceptions are mapped to these. */
enum class AppError {
    NO_INTERNET,
    UNAUTHORIZED,
    NOT_FOUND,
    SERVER,
    UNKNOWN,
}

/** Thrown where an API forces exceptions (e.g. Paging's LoadResult) – carries an [AppError]. */
class AppException(val error: AppError, cause: Throwable? = null) : Exception(error.name, cause)

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}
