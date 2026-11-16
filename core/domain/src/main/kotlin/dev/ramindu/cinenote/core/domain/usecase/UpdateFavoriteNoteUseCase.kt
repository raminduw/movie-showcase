package dev.ramindu.cinenote.core.domain.usecase

import dev.ramindu.cinenote.core.domain.repository.FavoritesReader
import dev.ramindu.cinenote.core.domain.repository.FavoritesWriter
import java.time.Clock
import javax.inject.Inject

sealed interface NoteUpdateResult {
    data object Saved : NoteUpdateResult
    data object Cleared : NoteUpdateResult
    data class TooLong(val maxLength: Int) : NoteUpdateResult
    data object NotAFavorite : NoteUpdateResult
}

/** Saves (or clears, when blank) the personal note on a favourite. */
class UpdateFavoriteNoteUseCase @Inject constructor(
    private val reader: FavoritesReader,
    private val writer: FavoritesWriter,
    private val clock: Clock,
) {
    suspend operator fun invoke(movieId: Int, note: String): NoteUpdateResult {
        val trimmed = note.trim()
        if (trimmed.length > MAX_NOTE_LENGTH) return NoteUpdateResult.TooLong(MAX_NOTE_LENGTH)
        if (reader.getFavorite(movieId) == null) return NoteUpdateResult.NotAFavorite

        return if (trimmed.isEmpty()) {
            writer.updateNote(movieId, note = null, updatedAt = null)
            NoteUpdateResult.Cleared
        } else {
            writer.updateNote(movieId, note = trimmed, updatedAt = clock.instant())
            NoteUpdateResult.Saved
        }
    }

    companion object {
        const val MAX_NOTE_LENGTH = 500
    }
}
