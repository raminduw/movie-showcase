package dev.ramindu.cinenote.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.ramindu.cinenote.core.designsystem.theme.CineNoteTheme

/** Multi-preview: renders the annotated composable in light and dark theme side by side. */
@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFFAF7F5)
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF0F1115,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class ThemePreviews

/** Same, at phone size – for full screens. */
@Preview(name = "Light – phone", showBackground = true, showSystemUi = true, device = "spec:width=411dp,height=891dp")
@Preview(
    name = "Dark – phone",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class ScreenPreviews

/** Theme + background wrapper for previews. */
@Composable
fun PreviewTheme(content: @Composable () -> Unit) {
    CineNoteTheme {
        Surface(color = MaterialTheme.colorScheme.background, content = content)
    }
}
