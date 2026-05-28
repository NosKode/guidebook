package com.guidebook.app.presentation.myplaces

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPhotoScreen(
    placeId:   String,
    onBack:    () -> Unit        = {},
    viewModel: AddPhotoViewModel = hiltViewModel()
) {
    val state    by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var caption  by remember { mutableStateOf("") }

    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onBack() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.pickImage(it) } }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
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
                    Text(
                        text       = "Добавить фото",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier            = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Фото-пикер ────────────────────────────────────────────
                PhotoPickerBox(
                    uri    = viewModel.selectedImageUri,
                    onPick = { imageLauncher.launch("image/*") }
                )

                // ── Подпись ───────────────────────────────────────────────
                Surface(
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(20.dp),
                    color           = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    tonalElevation  = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text       = "Подпись",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier   = Modifier.padding(bottom = 10.dp)
                        )
                        OutlinedTextField(
                            value         = caption,
                            onValueChange = { caption = it },
                            label         = { Text("Подпись (необязательно)") },
                            placeholder   = { Text("Опишите что на фото...") },
                            singleLine    = true,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier      = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ── Кнопка загрузки ───────────────────────────────────────
                Button(
                    onClick  = { viewModel.upload(caption) },
                    enabled  = !state.isLoading && viewModel.selectedImageUri != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Загрузить фото", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Компонент выбора фото ────────────────────────────────────────────────────

@Composable
private fun PhotoPickerBox(uri: Uri?, onPick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model              = uri,
                contentDescription = "Выбранное фото",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier           = Modifier.size(56.dp),
                    tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = "Нажмите, чтобы выбрать фото",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "JPG, PNG, WEBP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
