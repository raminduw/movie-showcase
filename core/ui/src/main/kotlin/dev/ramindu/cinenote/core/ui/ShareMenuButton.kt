package dev.ramindu.cinenote.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.ramindu.cinenote.core.designsystem.icon.CineIcons

/**
 * Share icon that opens a small menu: "Share…" (system share sheet) or "Copy link".
 * Used by the movie list cards and the detail header, so both behave the same.
 */
@Composable
fun ShareMenuButton(
    movieTitle: String,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    modifier: Modifier = Modifier,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }, colors = colors) {
            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.cd_share, movieTitle))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.share_action)) },
                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                onClick = {
                    expanded = false
                    onShare()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.share_copy_link)) },
                leadingIcon = { Icon(CineIcons.Link, contentDescription = null) },
                onClick = {
                    expanded = false
                    onCopyLink()
                },
            )
        }
    }
}
