package dev.ramindu.cinenote.core.ui

import java.util.Locale

/** Pure formatting helpers used by the presentation mappers (unit-tested, no Android). */
object Formatters {

    /** 7.567 → "7.6"; 0 → "–" (TMDB uses 0 for "not rated yet"). */
    fun rating(value: Double): String =
        if (value <= 0.0) "–" else String.format(Locale.US, "%.1f", value)

    /** 135 → "2h 15m", 45 → "45m", null → null. */
    fun runtime(minutes: Int?): String? {
        if (minutes == null || minutes <= 0) return null
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "${m}m"
            m == 0 -> "${h}h"
            else -> "${h}h ${m}m"
        }
    }

    fun year(year: Int?): String = year?.toString() ?: "TBA"

    /** " (1999 · ★ 8.4)", " (1999)", " (★ 8.4)" or "" – the bit after the title in a share message. */
    fun shareMeta(year: Int?, rating: Double): String {
        val parts = listOfNotNull(
            year?.toString(),
            if (rating > 0.0) "★ ${rating(rating)}" else null,
        )
        return if (parts.isEmpty()) "" else parts.joinToString(prefix = " (", separator = " · ", postfix = ")")
    }
}
