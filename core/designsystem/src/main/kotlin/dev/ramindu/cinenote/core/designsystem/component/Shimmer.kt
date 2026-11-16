package dev.ramindu.cinenote.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

/** Skeleton-loading sweep. Apply to placeholder boxes that mirror the real layout. */
@Composable
fun Modifier.shimmer(): Modifier {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surfaceContainerLow
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress = transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    return this.background(
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(x = progress.value * 600f, y = 0f),
            end = Offset(x = progress.value * 600f + 400f, y = 400f),
        ),
    )
}
