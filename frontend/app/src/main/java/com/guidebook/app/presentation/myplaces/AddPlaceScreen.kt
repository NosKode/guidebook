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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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

    // ── Поля формы ─────────────────────────────────────────────────────────
    var name                 by remember { mutableStateOf("") }
    var address              by remember { mutableStateOf("") }
    var latStr               by remember { mutableStateOf("") }
    var lonStr               by remember { mutableStateOf("") }
    var description          by remember { mutableStateOf("") }
    var selectedCategory     by remember { mutableStateOf(state.categories.firstOrNull()) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // Синхронизация категорий после загрузки
    LaunchedEffect(state.categories) {
        if (selectedCategory == null && state.categories.isNotEmpty()) {
            selectedCategory = state.categories.first()
        }
    }

    // Автозаполнение координат при возврате из пикера
    LaunchedEffect(pickedLat, pickedLon) {
        pickedLat?.let { latStr = "%.6f".format(it) }
        pickedLon?.let { lonStr = "%.6f".format(it) }
    }

    // Навигация назад при успехе
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onBack()
    }

    // Показ ошибок
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Выбор изображения
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.pickImage(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить место", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Обложка ──────────────────────────────────────────────────
            CoverPicker(
                uri    = viewModel.selectedImageUri,
                onPick = { imageLauncher.launch("image/*") }
            )

            // ── Название ─────────────────────────────────────────────────
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("Название *") },
                placeholder   = { Text("Введите название места") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Адрес ─────────────────────────────────────────────────────
            OutlinedTextField(
                value         = address,
                onValueChange = { address = it },
                label         = { Text("Адрес") },
                placeholder   = { Text("ул. Примерная, д. 1") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Координаты ───────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value           = latStr,
                    onValueChange   = { latStr = it },
                    label           = { Text("Широта") },
                    placeholder     = { Text("55.7522") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value           = lonStr,
                    onValueChange   = { lonStr = it },
                    label           = { Text("Долгота") },
                    placeholder     = { Text("37.6156") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.weight(1f)
                )
            }

            // ── Кнопка «Выбрать на карте» ─────────────────────────────
            OutlinedButton(
                onClick  = onPickCoordinates,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Map,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Выбрать координаты на карте")
            }

            // ── Категория ─────────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded        = categoryMenuExpanded,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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

            // ── Описание ─────────────────────────────────────────────────
            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("Описание") },
                placeholder   = { Text("Расскажите об этом месте...") },
                minLines      = 3,
                maxLines      = 6,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            // ── Кнопка отправки ───────────────────────────────────────────
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
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

// ── Компонент выбора обложки ─────────────────────────────────────────────────

@Composable
private fun CoverPicker(uri: Uri?, onPick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
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
                    modifier           = Modifier.size(40.dp),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Выбрать обложку",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
