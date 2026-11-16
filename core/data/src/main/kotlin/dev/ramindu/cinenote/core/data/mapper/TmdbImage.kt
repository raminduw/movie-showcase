package dev.ramindu.cinenote.core.data.mapper

/** Builds full image URLs from TMDB's relative paths (e.g. "/abc.jpg"). */
object TmdbImage {
    private const val BASE_URL = "https://image.tmdb.org/t/p/"

    fun poster(path: String?): String? = build(path, size = "w500")
    fun backdrop(path: String?): String? = build(path, size = "w1280")
    fun profile(path: String?): String? = build(path, size = "w185")

    private fun build(path: String?, size: String): String? =
        path?.takeIf { it.isNotBlank() }?.let { "$BASE_URL$size${if (it.startsWith('/')) it else "/$it"}" }
}
