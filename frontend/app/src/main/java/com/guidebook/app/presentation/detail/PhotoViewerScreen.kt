package com.guidebook.app.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    photoUrl: String,
    onBack: () -> Unit = {}
) {
    // ── Состояние pinch-to-zoom ───────────────────────────────────────────────
    var scale  by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale  = newScale
        // При возврате к масштабу 1× — сбрасываем смещение
        offset = if (newScale <= 1f) Offset.Zero else offset + panChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // ── Фотография ────────────────────────────────────────────────────────
        if (photoUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model              = photoUrl,
                contentDescription = null,
                contentScale       = ContentScale.Fit,
                modifier           = Modifier
                    .fillMaxSize()
                    .transformable(state = transformableState)
                    .graphicsLayer(
                        scaleX       = scale,
                        scaleY       = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                loading = {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color    = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            )
        } else {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Фото не найдено", color = Color.White)
            }
        }

        // ── Полупрозрачный TopAppBar поверх фото ──────────────────────────────
        TopAppBar(
            title          = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint               = Color.White
                    )
                }
            },
            colors   = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
