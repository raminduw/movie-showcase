package dev.ramindu.cinenote.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/** Network image with a shimmer while loading and a neutral fallback when there is no artwork. */
@Composable
fun MoviePoster(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    var isLoading by remember(url) { mutableStateOf(url != null) }
    // Previews can't load network images: show a tinted placeholder instead.
    val isPreview = LocalInspectionMode.current
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (isPreview && url != null) {
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary))
                } else {
                    SolidColor(MaterialTheme.colorScheme.surfaceVariant)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (url == null || isPreview) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(32.dp),
            )
        } else {
            if (isLoading) Box(Modifier.fillMaxSize().shimmer())
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                onSuccess = { isLoading = false },
                onError = { isLoading = false },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
