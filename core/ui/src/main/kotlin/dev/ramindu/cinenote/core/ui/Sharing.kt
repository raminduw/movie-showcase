package dev.ramindu.cinenote.core.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.ramindu.cinenote.core.domain.model.MovieShare

/**
 * Android-side sharing. ViewModels only emit "share this [MovieShare]" effects;
 * the screen calls these, so ViewModels stay free of Context and Intents (and easy to unit test).
 */

fun Context.movieShareText(share: MovieShare): String =
    getString(R.string.share_text, share.title, Formatters.shareMeta(share.releaseYear, share.rating), share.url)

/** Opens the system share sheet (WhatsApp, Gmail, Messages…). */
fun Context.openShareSheet(share: MovieShare) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, share.title)
        putExtra(Intent.EXTRA_TEXT, movieShareText(share))
    }
    val chooser = Intent.createChooser(send, getString(R.string.share_chooser_title, share.title))
    if (this !is android.app.Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(chooser)
}

/**
 * Copies the movie link.
 * @return true if the caller should confirm with its own message – Android 13+ already shows one.
 */
fun Context.copyMovieLink(share: MovieShare): Boolean {
    val clipboard = getSystemService(ClipboardManager::class.java) ?: return false
    clipboard.setPrimaryClip(ClipData.newPlainText(share.title, share.url))
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
}
