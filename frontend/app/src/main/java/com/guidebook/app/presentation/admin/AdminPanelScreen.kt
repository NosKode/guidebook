package com.guidebook.app.presentation.admin

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.guidebook.app.domain.model.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack:    () -> Unit          = {},
    viewModel: AdminPanelViewModel = hiltViewModel()
) {
    val state    by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var rejectTargetId by remember { mutableStateOf<String?>(null) }
    var rejectReason   by remember { mutableStateOf("") }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Header ────────────────────────────────────────────────────
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                color           = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                    Column {
                        Text(
                            text       = "Модерация",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.pendingPlaces.isNotEmpty()) {
                            Text(
                                text  = "${state.pendingPlaces.size} мест на рассмотрении",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                state.pendingPlaces.isEmpty() -> EmptyModerationState()

                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.pendingPlaces, key = { it.id }) { place ->
                        ModerationCard(
                            place     = place,
                            onApprove = { viewModel.approve(place.id) },
                            onReject  = { rejectTargetId = place.id }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    // ── Диалог отклонения ─────────────────────────────────────────────────────
    rejectTargetId?.let { targetId ->
        AlertDialog(
            onDismissRequest = { rejectTargetId = null; rejectReason = "" },
            shape  = RoundedCornerShape(24.dp),
            title  = { Text("Причина отклонения", fontWeight = FontWeight.SemiBold) },
            text   = {
                Column {
                    Text(
                        text  = "Укажите причину (необязательно). Она будет показана автору.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value         = rejectReason,
                        onValueChange = { rejectReason = it },
                        label         = { Text("Причина") },
                        placeholder   = { Text("Например: недостаточно информации") },
                        minLines      = 2,
                        maxLines      = 4,
                        shape         = RoundedCornerShape(14.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = MaterialTheme.colorScheme.error,
                            unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                            focusedContainerColor   = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier      = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reject(targetId, rejectReason.takeIf { it.isNotBlank() })
                        rejectTargetId = null
                        rejectReason   = ""
                    },
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Отклонить") }
            },
            dismissButton = {
                TextButton(onClick = { rejectTargetId = null; rejectReason = "" }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// ── Карточка места на модерации ──────────────────────────────────────────────

@Composable
private fun ModerationCard(
    place:     Place,
    onApprove: () -> Unit,
    onReject:  () -> Unit
) {
    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(20.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation  = 1.dp
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // ── Миниатюра обложки ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                if (place.coverUrl != null) {
                    AsyncImage(
                        model              = place.coverUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.AdminPanelSettings,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier           = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = place.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                if (!place.address.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = place.address,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!place.categoryName.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text       = place.categoryName,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick        = onApprove,
                        colors         = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape          = RoundedCornerShape(10.dp),
                        modifier       = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Одобрить", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick        = onReject,
                        colors         = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape          = RoundedCornerShape(10.dp),
                        modifier       = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Отклонить", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ── Пустое состояние ─────────────────────────────────────────────────────────

@Composable
private fun EmptyModerationState() {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Outlined.AdminPanelSettings,
                        contentDescription = null,
                        modifier           = Modifier.size(48.dp),
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text       = "Нет мест на модерации",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Все поданные места уже рассмотрены",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
