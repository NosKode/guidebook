package com.guidebook.app.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    var showLogoutDialog  by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.uploadAvatar(it) } }

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
            text    = { Text("Вы уверены, что хотите выйти?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout(onLogout) },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Выйти") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero header with gradient ──────────────────────────────────
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
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Avatar
                    Box(
                        modifier         = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model              = user!!.avatarUrl,
                                contentDescription = "Аватар",
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.size(100.dp).clip(CircleShape)
                            )
                        } else {
                            val initial = (user?.displayName?.firstOrNull()
                                ?: user?.email?.firstOrNull() ?: '?').uppercaseChar()
                            Box(
                                modifier         = Modifier
                                    .size(100.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = initial.toString(),
                                    fontSize   = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        // Camera overlay
                        Box(
                            modifier         = Modifier
                                .size(100.dp)
                                .background(Color.Black.copy(alpha = 0.22f), CircleShape),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (state.isUploadingAvatar) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(24.dp).padding(bottom = 8.dp),
                                    color       = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector        = Icons.Default.CameraAlt,
                                    contentDescription = "Сменить фото",
                                    tint               = Color.White,
                                    modifier           = Modifier.size(20.dp).padding(bottom = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

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
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isAdmin) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text       = if (isAdmin) "Администратор" else "Пользователь",
                            style      = MaterialTheme.typography.labelMedium,
                            modifier   = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            color      = if (isAdmin) MaterialTheme.colorScheme.onPrimary
                                         else MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Settings section ───────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SettingsCard {
                    SettingsRow(
                        icon  = if (state.isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        title = "Тёмная тема",
                        trailing = {
                            Switch(
                                checked         = state.isDarkTheme,
                                onCheckedChange = { viewModel.toggleTheme() }
                            )
                        }
                    )
                }

                if (isAdmin) {
                    SettingsCard {
                        SettingsRow(
                            icon    = Icons.Default.AdminPanelSettings,
                            title   = "Панель администратора",
                            onClick = onAdminPanel,
                            trailing = {
                                Icon(
                                    Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Surface(
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(20.dp),
                    color           = MaterialTheme.colorScheme.errorContainer,
                    onClick         = { showLogoutDialog = true }
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text       = "Выйти из аккаунта",
                            style      = MaterialTheme.typography.bodyLarge,
                            color      = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        color    = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation  = 1.dp
    ) {
        content()
    }
}

@Composable
private fun SettingsRow(
    icon:     ImageVector,
    title:    String,
    onClick:  (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(22.dp)
        )
        Text(
            text     = title,
            modifier = Modifier.weight(1f),
            style    = MaterialTheme.typography.bodyLarge
        )
        trailing()
    }
}
