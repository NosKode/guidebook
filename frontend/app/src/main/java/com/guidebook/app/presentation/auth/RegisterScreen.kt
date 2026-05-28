package com.guidebook.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email           by rememberSaveable { mutableStateOf("") }
    var password        by rememberSaveable { mutableStateOf("") }
    var displayName     by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    Scaffold(contentWindowInsets = WindowInsets(0)) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Gradient background ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(72.dp))

                // ── Logo ───────────────────────────────────────────────────
                Surface(
                    modifier        = Modifier.size(80.dp),
                    shape           = RoundedCornerShape(24.dp),
                    color           = MaterialTheme.colorScheme.primary,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector        = Icons.Filled.Explore,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onPrimary,
                            modifier           = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text       = "Путеводитель",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text  = "Создайте аккаунт",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(36.dp))

                // ── Form card ──────────────────────────────────────────────
                Surface(
                    modifier        = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape           = RoundedCornerShape(28.dp),
                    color           = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value         = displayName,
                            onValueChange = { displayName = it },
                            label         = { Text("Имя (необязательно)") },
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape  = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value         = email,
                            onValueChange = { email = it; viewModel.clearError() },
                            label         = { Text("Email") },
                            isError       = uiState.emailError != null,
                            supportingText = { uiState.emailError?.let { Text(it) } },
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape  = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value         = password,
                            onValueChange = { password = it; viewModel.clearError() },
                            label         = { Text("Пароль") },
                            isError       = uiState.passwordError != null,
                            supportingText = { uiState.passwordError?.let { Text(it) } },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible)
                                            Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction    = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.register(email, password, displayName)
                                }
                            ),
                            shape  = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AuthErrorBanner(message = uiState.error)

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.register(email, password, displayName)
                            },
                            enabled  = !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape  = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color       = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text       = "Зарегистрироваться",
                                    fontWeight = FontWeight.Bold,
                                    style      = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text  = "Уже есть аккаунт? ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text       = "Войти",
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
