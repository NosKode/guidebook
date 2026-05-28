package com.guidebook.app.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Switch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.guidebook.app.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onAdminPanel: () -> Unit       = {},
    onLogout:     () -> Unit       = {},
    viewModel:    ProfileViewModel = hiltViewModel()
) {
    val state   by viewModel.state.collectAsState()
    val user    = state.user
    val isAdmin = user?.role == UserRole.ADMIN

    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadAvatar(it) }
    }

    LaunchedEffect(state.avatarError) {
        state.avatarError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearAvatarError()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title   = { Text("Выйти из аккаунта?") },
            text    = { Text("Вы уверены, что хотите выйти? Потребуется повторный вход.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout(onLogout) },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Выйти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text       = "Профиль",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Выйти",
                            tint               = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ── Аватар + данные ───────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                // Кликабельный аватар с иконкой камеры
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (!user?.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model             = user!!.avatarUrl,
                            contentDescription = "Аватар",
                            contentScale      = ContentScale.Crop,
                            modifier          = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        val initial = (user?.displayName?.firstOrNull()
                            ?: user?.email?.firstOrNull()
                            ?: '?').uppercaseChar()
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = initial.toString(),
                                fontSize   = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Иконка камеры поверх аватара
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.Black.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isUploadingAvatar) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color    = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector        = Icons.Default.CameraAlt,
                                contentDescription = "Сменить фото",
                                tint               = Color.White,
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text       = user?.displayName ?: user?.email ?: "Пользователь",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!user?.displayName.isNullOrBlank() && user?.email != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text  = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isAdmin)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text     = if (isAdmin) "Администратор" else "Пользователь",
                            style    = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color    = if (isAdmin)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Тёмная тема ───────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = if (state.isDarkTheme) Icons.Outlined.DarkMode
                                             else Icons.Outlined.LightMode,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text     = "Тёмная тема",
                        modifier = Modifier.weight(1f),
                        style    = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked         = state.isDarkTheme,
                        onCheckedChange = { viewModel.toggleTheme() }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Меню (Панель администратора только для ADMIN) ─────────────────────
            if (isAdmin) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    ProfileMenuItem(
                        icon  = {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        title   = "Панель администратора",
                        onClick = onAdminPanel
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.weight(1f))

            // ── Кнопка выхода ─────────────────────────────────────────────
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick  = { showLogoutDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text("Выйти из аккаунта", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMenuItem(
    icon:    @Composable () -> Unit,
    title:   String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors  = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(16.dp))
            Text(text = title, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, contentDescription = null)
        }
    }
}
