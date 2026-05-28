package com.guidebook.app.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.guidebook.app.domain.model.Photo
import com.guidebook.app.presentation.components.ReviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: String,
    onBack: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    onAddPhoto: (String) -> Unit = {},
    viewModel: PlaceDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> FullScreenLoading()
        state.error != null && state.place == null -> ErrorScreen(
            message = state.error!!,
            onRetry = { viewModel.loadAll() },
            onBack  = onBack
        )
        state.place != null -> DetailContent(
            state          = state,
            placeId        = placeId,
            onBack         = onBack,
            onPhotoClick   = onPhotoClick,
            onAddPhoto     = { onAddPhoto(placeId) },
            onFavorite     = { viewModel.toggleFavorite() },
            onCreateReview = { rating, comment -> viewModel.createReview(rating, comment) },
            onDeleteReview = { viewModel.deleteReview(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(
    state: DetailUiState,
    placeId: String,
    onBack: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onFavorite: () -> Unit,
    onCreateReview: (Int, String?) -> Unit,
    onDeleteReview: (String) -> Unit
) {
    val place  = state.place!!
    val haptic = LocalHapticFeedback.current
    var descriptionExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ── Hero image — 4:3 for stronger visual weight ────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                ) {
                    if (place.coverUrl != null) {
                        SubcomposeAsyncImage(
                            model              = place.coverUrl,
                            contentDescription = place.name,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize(),
                            loading            = { HeroPlaceholder() },
                            error              = { HeroPlaceholder() }
                        )
                    } else {
                        HeroPlaceholder()
                    }
                    // Top gradient for controls readability
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Black.copy(alpha = 0.55f),
                                    1f to Color.Transparent
                                )
                            )
                    )
                    // Bottom gradient for title overlap
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f    to Color.Transparent,
                                    0.55f to Color.Transparent,
                                    1f    to Color.Black.copy(alpha = 0.75f)
                                )
                            )
                    )
                    // Title + category overlaid on hero bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        if (!place.categoryName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text     = place.categoryName,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(
                            text       = place.name,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            maxLines   = 2,
                            overflow   = TextOverflow.Ellipsis
                        )
                        if (!place.address.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint     = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text  = place.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // ── Stat cards row ─────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (place.averageRating > 0) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon     = Icons.Filled.Star,
                            iconTint = Color(0xFFF59E0B),
                            value    = "%.1f".format(place.averageRating),
                            label    = "Рейтинг"
                        )
                    }
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Outlined.RateReview,
                        value    = "${place.reviewsCount}",
                        label    = "Отзывов"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Filled.CameraAlt,
                        value    = "${place.photosCount}",
                        label    = "Фото"
                    )
                }
            }

            // ── Add photo button ───────────────────────────────────────────
            if (state.isCurrentUserOwner || state.isAdmin) {
                item {
                    OutlinedButton(
                        onClick  = onAddPhoto,
                        shape    = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить фото")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Description ────────────────────────────────────────────────
            if (!place.description.isNullOrBlank()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(
                            text       = "Описание",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text     = place.description,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (descriptionExpanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (place.description.length > 200) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text     = if (descriptionExpanded) "Свернуть" else "Читать далее",
                                style    = MaterialTheme.typography.labelMedium,
                                color    = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { descriptionExpanded = !descriptionExpanded }
                            )
                        }
                    }
                }
            }

            // ── Photos ─────────────────────────────────────────────────────
            if (state.photos.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Text(
                            text       = "Фотографии",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.photos) { photo ->
                                PhotoThumbnail(
                                    photo   = photo,
                                    onClick = { onPhotoClick(photo.photoUrl) }
                                )
                            }
                        }
                    }
                }
            }

            // ── Reviews section header ─────────────────────────────────────
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Text(
                    text       = "Отзывы (${state.reviews.size})",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            // ── Review form ────────────────────────────────────────────────
            if (state.currentUserReview == null) {
                item {
                    ReviewForm(
                        isSubmitting = state.isSubmittingReview,
                        error        = state.reviewError,
                        onSubmit     = onCreateReview
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Reviews list ───────────────────────────────────────────────
            if (state.reviews.isEmpty() && state.currentUserReview == null) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "Отзывов ещё нет. Будьте первым!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.reviews, key = { it.id }) { review ->
                    ReviewCard(
                        review    = review,
                        canDelete = state.isAdmin || review.userId == state.currentUserId,
                        onDelete  = onDeleteReview
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }

        // ── Overlay controls ───────────────────────────────────────────────
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint               = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick  = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFavorite()
                    },
                    enabled  = !state.isTogglingFavorite,
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                ) {
                    if (state.isTogglingFavorite) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector        = if (state.isFavorite) Icons.Filled.Favorite
                                                 else Icons.Outlined.FavoriteBorder,
                            contentDescription = "В избранное",
                            tint               = if (state.isFavorite) Color(0xFFFF6B6B) else Color.White
                        )
                    }
                }
            },
            colors   = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Review form ───────────────────────────────────────────────────────────────

@Composable
private fun ReviewForm(
    isSubmitting: Boolean,
    error: String?,
    onSubmit: (rating: Int, comment: String?) -> Unit
) {
    var selectedRating by rememberSaveable { mutableIntStateOf(0) }
    var comment by rememberSaveable { mutableStateOf("") }

    Surface(
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape          = RoundedCornerShape(20.dp),
        color          = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text       = "Оставить отзыв",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < selectedRating) Icons.Filled.Star
                                      else Icons.Outlined.StarOutline,
                        contentDescription = "${index + 1} звезда",
                        tint = if (index < selectedRating) Color(0xFFF59E0B)
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { selectedRating = index + 1 }
                    )
                }
            }

            OutlinedTextField(
                value         = comment,
                onValueChange = { comment = it },
                placeholder   = {
                    Text(
                        "Поделитесь впечатлениями...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier  = Modifier.fillMaxWidth(),
                minLines  = 2,
                maxLines  = 5,
                shape     = RoundedCornerShape(14.dp),
                colors    = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (error != null) {
                Text(
                    text  = error,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick  = { onSubmit(selectedRating, comment.takeIf { it.isNotBlank() }) },
                enabled  = selectedRating > 0 && !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Отправить отзыв", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    value:    String,
    label:    String,
    icon:     ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier       = modifier,
        shape          = RoundedCornerShape(16.dp),
        color          = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(22.dp)
            )
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(photo: Photo, onClick: () -> Unit) {
    SubcomposeAsyncImage(
        model              = photo.photoUrl,
        contentDescription = photo.caption,
        contentScale       = ContentScale.Crop,
        modifier           = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        loading = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    )
}

@Composable
private fun HeroPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
    )
}

@Composable
private fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            }
        )
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("😕", style = MaterialTheme.typography.displayMedium)
                Text(
                    text       = "Не удалось загрузить",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onRetry) { Text("Повторить") }
            }
        }
    }
}
