package dev.ramindu.cinenote.core.domain.model

/** Everything needed to share a movie. How it's worded is the UI's job; what to link to is ours. */
data class MovieShare(
    val title: String,
    val releaseYear: Int?,
    val rating: Double,
    val url: String,
)
