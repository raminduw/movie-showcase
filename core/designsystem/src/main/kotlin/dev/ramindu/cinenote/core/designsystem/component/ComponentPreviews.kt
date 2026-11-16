package dev.ramindu.cinenote.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.ramindu.cinenote.core.designsystem.icon.CineIcons
import dev.ramindu.cinenote.core.designsystem.preview.PreviewTheme
import dev.ramindu.cinenote.core.designsystem.preview.ThemePreviews

/** Previews for the reusable building blocks (light + dark via @ThemePreviews). */

@ThemePreviews
@Composable
private fun MoviePosterPreview() = PreviewTheme {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box {
            MoviePoster(url = "https://image.tmdb.org/t/p/w500/x.jpg", contentDescription = null, modifier = Modifier.width(92.dp).height(138.dp))
            RatingBadge("8.4", Modifier.align(Alignment.BottomStart).padding(6.dp))
        }
        // No artwork fallback
        MoviePoster(url = null, contentDescription = null, modifier = Modifier.width(92.dp).height(138.dp))
        // Cast avatar
        MoviePoster(url = null, contentDescription = null, shape = CircleShape, modifier = Modifier.size(72.dp))
    }
}

@ThemePreviews
@Composable
private fun RatingBadgePreview() = PreviewTheme {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RatingBadge("8.4")
        RatingBadge("6.1")
        RatingBadge("–")
    }
}

@ThemePreviews
@Composable
private fun FavoriteButtonPreview() = PreviewTheme {
    Row(Modifier.padding(8.dp)) {
        FavoriteButton(isFavorite = false, onClick = {}, contentDescription = "Add to favourites")
        FavoriteButton(isFavorite = true, onClick = {}, contentDescription = "Remove from favourites")
    }
}

@ThemePreviews
@Composable
private fun SearchFieldPreview() = PreviewTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SearchField(query = "", onQueryChange = {}, placeholder = "Search all movies", clearContentDescription = "Clear", modifier = Modifier.fillMaxWidth())
        SearchField(query = "Dune", onQueryChange = {}, placeholder = "Search all movies", clearContentDescription = "Clear", modifier = Modifier.fillMaxWidth())
    }
}

@ThemePreviews
@Composable
private fun EmptyStatePreview() = PreviewTheme {
    EmptyState(
        icon = Icons.Filled.Favorite,
        title = "No favourites yet",
        message = "Tap the heart on any movie to keep it here – then add a note to remember why.",
        actionLabel = "Browse movies",
        onAction = {},
    )
}

@ThemePreviews
@Composable
private fun ErrorStatePreview() = PreviewTheme {
    ErrorState(
        title = "Oops!",
        message = "You seem to be offline. Check your connection and try again.",
        retryLabel = "Try again",
        onRetry = {},
    )
}

@ThemePreviews
@Composable
private fun ShimmerPreview() = PreviewTheme {
    Row(Modifier.padding(16.dp)) {
        Box(Modifier.width(92.dp).height(138.dp).clip(RoundedCornerShape(12.dp)).shimmer())
        Column(Modifier.padding(start = 12.dp)) {
            Box(Modifier.width(160.dp).height(18.dp).clip(RoundedCornerShape(8.dp)).shimmer())
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(48.dp).height(14.dp).clip(RoundedCornerShape(8.dp)).shimmer())
        }
    }
}

@ThemePreviews
@Composable
private fun IconsPreview() = PreviewTheme {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Filled.Share, contentDescription = null)
        Icon(CineIcons.Link, contentDescription = null)
    }
}
