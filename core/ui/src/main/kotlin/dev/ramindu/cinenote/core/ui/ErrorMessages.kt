package dev.ramindu.cinenote.core.ui

import androidx.annotation.StringRes
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppException

@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.NO_INTERNET -> R.string.error_no_internet
    AppError.UNAUTHORIZED -> R.string.error_unauthorized
    AppError.NOT_FOUND -> R.string.error_not_found
    AppError.SERVER -> R.string.error_server
    AppError.UNKNOWN -> R.string.error_unknown
}

/** Paging reports failures as Throwables; recover the AppError we put there in the data layer. */
fun Throwable.toAppError(): AppError = (this as? AppException)?.error ?: AppError.UNKNOWN
