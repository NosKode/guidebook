package com.guidebook.app.presentation.favorites

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.guidebook.app.domain.model.Place
import com.guidebook.app.presentation.components.EmptyState
import com.guidebook.app.presentation.components.ErrorMessage
import com.guidebook.app.presentation.components.PlaceCard
import com.guidebook.app.presentation.components.ShimmerPlaceGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onPlaceClick : (String) -> Unit = {},
    viewModel    : FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    Scaffold(contentWindowInsets = WindowInsets(0)) { _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Hero header ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text       = "Избранное",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (favorites.isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text  = "${favorites.size} ${pluralPlaces(favorites.size)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            when {
                syncState.isLoading && favorites.isEmpty() -> {
                    ShimmerPlaceGrid()
                }

                syncState.error != null && favorites.isEmpty() -> {
                    ErrorMessage(
                        message = syncState.error!!,
                        onRetry = { viewModel.syncWithServer() }
                    )
                }

                favorites.isEmpty() -> {
                    EmptyState(
                        icon     = Icons.Outlined.FavoriteBorder,
                        title    = "Нет избранных мест",
                        subtitle = "Добавляйте места в избранное,\nчтобы быстро их находить"
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns               = GridCells.Fixed(2),
                        modifier              = Modifier.fillMaxSize(),
                        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement   = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = favorites,
                            key   = { it.id }
                        ) { place ->
                            SwipeableFavoriteCard(
                                place        = place,
                                onPlaceClick = onPlaceClick,
                                onRemove     = { viewModel.removeFavorite(place.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun pluralPlaces(count: Int): String = when {
    count % 100 in 11..19 -> "мест"
    count % 10 == 1        -> "место"
    count % 10 in 2..4     -> "места"
    else                   -> "мест"
}

// ── Карточка со свайпом ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableFavoriteCard(
    place        : Place,
    onPlaceClick : (String) -> Unit,
    onRemove     : () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            value == SwipeToDismissBoxValue.EndToStart
        }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onRemove()
        }
    }

    SwipeToDismissBox(
        state                       = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            DeleteBackground(fraction = dismissState.progress)
        }
    ) {
        PlaceCard(
            place   = place,
            onClick = { onPlaceClick(place.id) }
        )
    }
}

// ── Красный фон при свайпе ───────────────────────────────────────────────────

@Composable
private fun DeleteBackground(fraction: Float) {
    val color by animateColorAsState(
        targetValue = if (fraction > 0.1f)
            MaterialTheme.colorScheme.errorContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "swipe_bg"
    )

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(color),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector        = Icons.Default.Delete,
            contentDescription = "Удалить из избранного",
            tint               = MaterialTheme.colorScheme.onErrorContainer,
            modifier           = Modifier
                .padding(end = 16.dp)
                .size(24.dp)
        )
    }
}
