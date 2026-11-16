package dev.ramindu.cinenote.core.designsystem.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Shared-element plumbing. :app provides both scopes; features just call [sharedPoster].
 * When either scope is missing (previews, tests) the modifier is a no-op.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

fun posterSharedKey(movieId: Int) = "poster-$movieId"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPoster(movieId: Int): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@sharedPoster.sharedElement(
            sharedContentState = rememberSharedContentState(key = posterSharedKey(movieId)),
            animatedVisibilityScope = visibilityScope,
        )
    }
}
