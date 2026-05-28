package com.guidebook.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.model.PlaceStatus

@Composable
fun PlaceCard(
    place: Place,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showStatusBadge: Boolean = false
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // ── Cover image with overlaid chips ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                if (place.coverUrl != null) {
                    SubcomposeAsyncImage(
                        model              = place.coverUrl,
                        contentDescription = place.name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                        loading            = { CoverPlaceholder() },
                        error              = { CoverPlaceholder(isError = true) }
                    )
                } else {
                    CoverPlaceholder()
                }

                // Gradient for chip readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f    to Color.Transparent,
                                0.45f to Color.Transparent,
                                1f    to Color.Black.copy(alpha = 0.72f)
                            )
                        )
                )

                // Status badge — top-end (PENDING / REJECTED)
                if (showStatusBadge && place.status != PlaceStatus.APPROVED) {
                    StatusBadge(
                        status   = place.status,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    )
                }

                // Category chip — bottom-start
                if (!place.categoryName.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.93f)
                    ) {
                        Text(
                            text       = place.categoryName,
                            modifier   = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Rating chip — bottom-end
                if (place.averageRating > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(100.dp),
                        color = Color.Black.copy(alpha = 0.52f)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Star,
                                contentDescription = null,
                                tint               = Color(0xFFFBBF24),
                                modifier           = Modifier.size(12.dp)
                            )
                            Text(
                                text       = "%.1f".format(place.averageRating),
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }
                    }
                }
            }

            // ── Info section ─────────────────────────────────────────────────
            Column(
                modifier            = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text       = place.name,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                if (!place.address.isNullOrBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(13.dp)
                        )
                        Text(
                            text     = if (place.reviewsCount > 0)
                                "${place.address} · ${place.reviewsCount} отз."
                            else
                                place.address,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else if (place.reviewsCount > 0) {
                    Text(
                        text  = "${place.reviewsCount} отзывов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun CoverPlaceholder(isError: Boolean = false) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (isError) {
            Icon(
                imageVector        = Icons.Default.BrokenImage,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier           = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun StatusBadge(status: PlaceStatus, modifier: Modifier = Modifier) {
    val (label, bgColor) = when (status) {
        PlaceStatus.PENDING  -> "На проверке" to Color(0xFFFFA726)
        PlaceStatus.REJECTED -> "Отклонено"   to Color(0xFFEF5350)
        PlaceStatus.APPROVED -> return
    }
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(100.dp),
        color    = bgColor.copy(alpha = 0.92f)
    ) {
        Text(
            text       = label,
            modifier   = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            color      = Color.White,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
