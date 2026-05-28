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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    onBack:            () -> Unit        = {},
    onPickCoordinates: () -> Unit        = {},
    pickedLat:         Double?           = null,
    pickedLon:         Double?           = null,
    viewModel:         AddPlaceViewModel = hiltViewModel()
) {
    val state    by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var name                 by remember { mutableStateOf("") }
    var address              by remember { mutableStateOf("") }
    var latStr               by remember { mutableStateOf("") }
    var lonStr               by remember { mutableStateOf("") }
    var description          by remember { mutableStateOf("") }
    var selectedCategory     by remember { mutableStateOf(state.categories.firstOrNull()) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.categories) {
        if (selectedCategory == null && state.categories.isNotEmpty()) {
            selectedCategory = state.categories.first()
        }
    }
    LaunchedEffect(pickedLat, pickedLon) {
        pickedLat?.let { latStr = "%.6f".format(it) }
        pickedLon?.let { lonStr = "%.6f".format(it) }
    }
    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onBack() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
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
                        text       = "Добавить место",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier            = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Обложка ───────────────────────────────────────────────
                CoverPicker(
                    uri    = viewModel.selectedImageUri,
                    onPick = { imageLauncher.launch("image/*") }
                )

                // ── Основная информация ────────────────────────────────────
                FormSection(title = "Основная информация") {
                    FormTextField(
                        value         = name,
                        onValueChange = { name = it },
                        label         = "Название *",
                        placeholder   = "Введите название места"
                    )

                    ExposedDropdownMenuBox(
                        expanded         = categoryMenuExpanded,
                        onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value         = selectedCategory?.name ?: "Выберите категорию",
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Категория") },
                            trailingIcon  = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                            },
                            shape  = RoundedCornerShape(14.dp),
                            colors = formFieldColors(),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded         = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            state.categories.forEach { category ->
                                DropdownMenuItem(
                                    text    = { Text(category.name) },
                                    onClick = {
                                        selectedCategory     = category
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ── Расположение ───────────────────────────────────────────
                FormSection(title = "Расположение") {
                    FormTextField(
                        value         = address,
                        onValueChange = { address = it },
                        label         = "Адрес",
                        placeholder   = "ул. Примерная, д. 1"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value           = latStr,
                            onValueChange   = { latStr = it },
                            label           = { Text("Широта") },
                            placeholder     = { Text("55.7522") },
                            singleLine      = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape           = RoundedCornerShape(14.dp),
                            colors          = formFieldColors(),
                            modifier        = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value           = lonStr,
                            onValueChange   = { lonStr = it },
                            label           = { Text("Долгота") },
                            placeholder     = { Text("37.6156") },
                            singleLine      = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape           = RoundedCornerShape(14.dp),
                            colors          = formFieldColors(),
                            modifier        = Modifier.weight(1f)
                        )
                    }
                    OutlinedButton(
                        onClick  = onPickCoordinates,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Map,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Выбрать на карте")
                    }
                }

                // ── Описание ───────────────────────────────────────────────
                FormSection(title = "Описание") {
                    OutlinedTextField(
                        value         = description,
                        onValueChange = { description = it },
                        label         = { Text("Описание") },
                        placeholder   = { Text("Расскажите об этом месте...") },
                        minLines      = 3,
                        maxLines      = 6,
                        shape         = RoundedCornerShape(14.dp),
                        colors        = formFieldColors(),
                        modifier      = Modifier.fillMaxWidth()
                    )
                }

                // ── Submit ─────────────────────────────────────────────────
                Button(
                    onClick = {
                        viewModel.submit(
                            name        = name,
                            address     = address.takeIf { it.isNotBlank() },
                            latitude    = latStr.toDoubleOrNull(),
                            longitude   = lonStr.toDoubleOrNull(),
                            categoryId  = selectedCategory?.id,
                            description = description.takeIf { it.isNotBlank() }
                        )
                    },
                    enabled  = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Добавить место", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

// ── Секция формы с заголовком ────────────────────────────────────────────────

@Composable
private fun FormSection(
    title:   String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(20.dp),
            color           = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            tonalElevation  = 1.dp
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

// ── Поле формы (shortcut) ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTextField(
    value:         String,
    onValueChange: (String) -> Unit,
    label:         String,
    placeholder:   String
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        placeholder   = { Text(placeholder) },
        singleLine    = true,
        shape         = RoundedCornerShape(14.dp),
        colors        = formFieldColors(),
        modifier      = Modifier.fillMaxWidth()
    )
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
    focusedContainerColor   = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)

// ── Компонент выбора обложки ─────────────────────────────────────────────────

@Composable
private fun CoverPicker(uri: Uri?, onPick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
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
                contentDescription = "Обложка места",
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
                    modifier           = Modifier.size(44.dp),
                    tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text       = "Выбрать обложку",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
