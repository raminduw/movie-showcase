package dev.ramindu.cinenote.core.ui

import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.domain.result.AppError
import dev.ramindu.cinenote.core.domain.result.AppException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

class FormattersTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource("7.567,7.6", "8.0,8.0", "10.0,10.0", "0.0,–", "-1.0,–")
    fun `rating is one decimal or a dash when unrated`(input: Double, expected: String) {
        assertThat(Formatters.rating(input)).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0} min -> {1}")
    @CsvSource("135,2h 15m", "45,45m", "120,2h", "61,1h 1m")
    fun `runtime is human readable`(minutes: Int, expected: String) {
        assertThat(Formatters.runtime(minutes)).isEqualTo(expected)
    }

    @Test
    fun `missing or zero runtime is null`() {
        assertThat(Formatters.runtime(null)).isNull()
        assertThat(Formatters.runtime(0)).isNull()
    }

    @Test
    fun `unknown year shows TBA`() {
        assertThat(Formatters.year(null)).isEqualTo("TBA")
        assertThat(Formatters.year(1999)).isEqualTo("1999")
    }

    @Test
    fun `share meta shows year and rating when available`() {
        assertThat(Formatters.shareMeta(1999, 8.43)).isEqualTo(" (1999 · ★ 8.4)")
        assertThat(Formatters.shareMeta(1999, 0.0)).isEqualTo(" (1999)")
        assertThat(Formatters.shareMeta(null, 7.0)).isEqualTo(" (★ 7.0)")
        assertThat(Formatters.shareMeta(null, 0.0)).isEmpty()
    }

    @ParameterizedTest
    @EnumSource(AppError::class)
    fun `every AppError has a message`(error: AppError) {
        assertThat(error.messageRes()).isNotEqualTo(0)
    }

    @Test
    fun `throwable to AppError unwraps AppException and defaults to UNKNOWN`() {
        assertThat(AppException(AppError.SERVER).toAppError()).isEqualTo(AppError.SERVER)
        assertThat(IllegalStateException().toAppError()).isEqualTo(AppError.UNKNOWN)
    }
}
