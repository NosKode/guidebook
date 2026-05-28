package com.guidebook.app.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateToMain:  () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsState()

    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.72f) }

    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, animationSpec = tween(500)) }
        scale.animateTo(1f, animationSpec = tween(600, easing = EaseOutBack))
    }

    LaunchedEffect(destination) {
        when (destination) {
            SplashViewModel.Destination.MAIN  -> onNavigateToMain()
            SplashViewModel.Destination.LOGIN -> onNavigateToLogin()
            null -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f   to MaterialTheme.colorScheme.primaryContainer,
                    0.4f to MaterialTheme.colorScheme.background,
                    1f   to MaterialTheme.colorScheme.background
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .alpha(alpha.value)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier        = Modifier.size(100.dp),
                shape           = RoundedCornerShape(28.dp),
                color           = MaterialTheme.colorScheme.primary,
                shadowElevation = 16.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Filled.Explore,
                        contentDescription = null,
                        modifier           = Modifier.size(56.dp),
                        tint               = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text       = "Путеводитель",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text  = "Открой мир вокруг себя",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        CircularProgressIndicator(
            modifier    = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .alpha(alpha.value),
            color       = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            strokeWidth = 2.dp
        )
    }
}
