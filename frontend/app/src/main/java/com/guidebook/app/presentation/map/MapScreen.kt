package com.guidebook.app.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.guidebook.app.domain.model.Place
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map as YandexMap
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import android.graphics.PointF
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults

// ── Цвета категорий ──────────────────────────────────────────────────────────

internal val CATEGORY_COLORS = mapOf(
    1 to 0xFFE53935.toInt(),
    2 to 0xFF1565C0.toInt(),
    3 to 0xFF6A1B9A.toInt(),
    4 to 0xFF2E7D32.toInt(),
    5 to 0xFFEF6C00.toInt(),
    6 to 0xFF00695C.toInt(),
    7 to 0xFFAD1457.toInt(),
    8 to 0xFF283593.toInt()
)

internal val DEFAULT_MARKER_COLOR = 0xFF455A64.toInt()

// ── Пин-маркер (кружок + хвостик) ────────────────────────────────────────────
//
//   Anchor выставляется на кончик хвостика (0.5, 1.0), чтобы он точно
//   указывал на координату места.

internal fun createPinBitmap(color: Int, widthPx: Int, photo: Bitmap? = null): Bitmap {
    val headRadius  = widthPx / 2f
    val totalHeight = (widthPx * 1.6f).toInt()
    val bmp    = Bitmap.createBitmap(widthPx, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val cx     = widthPx / 2f
    val cy     = headRadius

    // Тень
    canvas.drawCircle(cx + 2f, cy + 2f, headRadius - 1f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(60, 0, 0, 0)
    })

    // Белое кольцо (внешнее)
    canvas.drawCircle(cx, cy, headRadius - 1f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
    })

    // Цветное кольцо категории
    canvas.drawCircle(cx, cy, headRadius - 5f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
    })

    // Хвостик
    canvas.drawPath(
        Path().apply {
            moveTo(cx - headRadius * 0.35f, cy + headRadius * 0.7f)
            lineTo(cx + headRadius * 0.35f, cy + headRadius * 0.7f)
            lineTo(cx, totalHeight.toFloat() - 1f)
            close()
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    )

    if (photo != null && photo.width > 0 && photo.height > 0) {
        // Фото-кружок поверх цветного кольца
        val photoRadius  = (headRadius - 9f).coerceAtLeast(4f)
        val photoDiameter = (photoRadius * 2).toInt()
        val scaled = Bitmap.createScaledBitmap(photo, photoDiameter, photoDiameter, true)
        val shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).also {
            it.setLocalMatrix(Matrix().apply { setTranslate(cx - photoRadius, cy - photoRadius) })
        }
        canvas.drawCircle(cx, cy, photoRadius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.shader = shader
        })
    } else {
        // Запасной вариант: белая точка (оригинальный дизайн)
        canvas.drawCircle(cx, cy, headRadius * 0.22f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.WHITE
        })
    }

    return bmp
}

// Стиль иконки: якорь на кончике хвостика
private val PIN_ICON_STYLE = IconStyle().apply {
    setAnchor(PointF(0.5f, 1.0f))
}

// ── Главный экран карты ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onPlaceClick: (String) -> Unit = {},
    viewModel:    MapViewModel     = hiltViewModel()
) {
    val state         by viewModel.state.collectAsState()
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density        = LocalDensity.current
    val snackbar       = remember { SnackbarHostState() }

    val mapView         = remember { MapView(context) }
    val tapListeners    = remember { mutableListOf<MapObjectTapListener>() }
    val userLocationRef = remember { mutableStateOf<UserLocationLayer?>(null) }
    val pinWidthPx      = with(density) { 36.dp.toPx().toInt() }

    // InputListener: тап по пустой области карты → снимаем выделение
    val mapInputListener = remember {
        object : InputListener {
            override fun onMapTap(map: YandexMap, point: Point) {
                viewModel.selectPlace(null)
            }
            override fun onMapLongTap(map: YandexMap, point: Point) {}
        }
    }

    // ── Жизненный цикл MapKit + cleanup InputListener ─────────────────────
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }
                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            runCatching { mapView.mapWindow.map.removeInputListener(mapInputListener) }
        }
    }

    // ── Запрос геолокации ─────────────────────────────────────────────────
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) runCatching {
            val layer = MapKitFactory.getInstance()
                .createUserLocationLayer(mapView.mapWindow)
            layer.isVisible       = true
            layer.isHeadingEnabled = false
            userLocationRef.value  = layer
            layer.cameraPosition()?.let { pos ->
                mapView.mapWindow.map.move(
                    pos,
                    com.yandex.mapkit.Animation(
                        com.yandex.mapkit.Animation.Type.SMOOTH, 1.5f
                    ),
                    null
                )
            }
        }
    }

    // ── Ошибки ────────────────────────────────────────────────────────────
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    // ── Инициализация камеры и InputListener ──────────────────────────────
    LaunchedEffect(Unit) {
        mapView.mapWindow.map.move(
            CameraPosition(Point(55.7522, 37.6156), 11f, 0f, 0f)
        )
        mapView.mapWindow.map.addInputListener(mapInputListener)
    }

    // ── Фокус камеры при поиске / тапе по маркеру ────────────────────────
    LaunchedEffect(Unit) {
        viewModel.focusEvent.collect { place ->
            val lat = place.latitude  ?: return@collect
            val lon = place.longitude ?: return@collect
            mapView.mapWindow.map.move(
                CameraPosition(Point(lat, lon), 16f, 0f, 0f),
                com.yandex.mapkit.Animation(
                    com.yandex.mapkit.Animation.Type.SMOOTH, 1f
                ),
                null
            )
        }
    }

    // ── Обновление маркеров при смене списка мест ─────────────────────────
    LaunchedEffect(state.filteredPlaces) {
        val map = mapView.mapWindow.map
        map.mapObjects.clear()
        tapListeners.clear()

        // Загружаем обложки всех мест параллельно
        val photos: List<Bitmap?> = state.filteredPlaces.map { place ->
            async {
                place.coverUrl?.let { url ->
                    runCatching {
                        (context.imageLoader.execute(
                            ImageRequest.Builder(context)
                                .data(url)
                                .allowHardware(false)   // нужен software-bitmap для BitmapShader
                                .size(pinWidthPx * 2, pinWidthPx * 2)
                                .build()
                        ).drawable as? BitmapDrawable)?.bitmap
                    }.getOrNull()
                }
            }
        }.awaitAll()

        state.filteredPlaces.forEachIndexed { index, place ->
            val lat   = place.latitude  ?: return@forEachIndexed
            val lon   = place.longitude ?: return@forEachIndexed
            val color = CATEGORY_COLORS[place.categoryId] ?: DEFAULT_MARKER_COLOR
            val bmp   = createPinBitmap(color, pinWidthPx, photos[index])

            val pm = map.mapObjects.addPlacemark().apply { geometry = Point(lat, lon) }
            pm.setIcon(ImageProvider.fromBitmap(bmp), PIN_ICON_STYLE)

            // MapKit держит слабую ссылку — список tapListeners обеспечивает strong ref
            val listener = MapObjectTapListener { _, _ ->
                viewModel.selectAndFocus(place)
                true
            }
            pm.addTapListener(listener)
            tapListeners.add(listener)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Layout ────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {

        // Карта (полный экран, под всеми оверлеями)
        AndroidView(
            factory  = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Единая панель: поиск + чипы (сверху, фон уходит за статус-бар)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                // ── Строка поиска ─────────────────────────────────────────
                TextField(
                    value         = state.searchQuery,
                    onValueChange = { viewModel.search(it) },
                    placeholder   = { Text("Поиск мест...") },
                    leadingIcon   = {
                        Icon(Icons.Default.Search, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon  = if (state.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.search("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    colors   = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                // ── Фильтр-чипы категорий ─────────────────────────────────
                if (state.categories.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    LazyRow(
                        modifier              = Modifier.fillMaxWidth(),
                        contentPadding        = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedCategoryId == null,
                                onClick  = { viewModel.filterByCategory(null) },
                                label    = { Text("Все") },
                                colors   = FilterChipDefaults.filterChipColors(
                                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        items(state.categories) { category ->
                            val isSelected = state.selectedCategoryId == category.id
                            val chipColor  = Color(CATEGORY_COLORS[category.id] ?: DEFAULT_MARKER_COLOR)
                            FilterChip(
                                selected = isSelected,
                                onClick  = {
                                    viewModel.filterByCategory(if (isSelected) null else category.id)
                                },
                                label  = { Text(category.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedContainerColor = chipColor,
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // ── Выпадающий список подсказок (под панелью) ─────────────────
            AnimatedVisibility(
                visible = state.searchSuggestions.isNotEmpty(),
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 4.dp),
                    shape     = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        state.searchSuggestions.forEachIndexed { index, place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectAndFocus(place) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector        = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    modifier           = Modifier.size(18.dp),
                                    tint               = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text       = place.name,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines   = 1,
                                        overflow   = TextOverflow.Ellipsis
                                    )
                                    if (!place.address.isNullOrBlank()) {
                                        Text(
                                            text     = place.address,
                                            style    = MaterialTheme.typography.bodySmall,
                                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            if (index < state.searchSuggestions.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Индикатор загрузки (по центру)
        AnimatedVisibility(
            visible  = state.isLoading,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                shape     = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(20.dp),
                    color    = MaterialTheme.colorScheme.primary
                )
            }
        }

        // FAB «Моё местоположение» (правый нижний угол)
        FloatingActionButton(
            onClick = {
                val hasPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPerm) {
                    runCatching {
                        val layer = userLocationRef.value
                            ?: MapKitFactory.getInstance()
                                .createUserLocationLayer(mapView.mapWindow)
                                .also {
                                    it.isVisible       = true
                                    it.isHeadingEnabled = false
                                    userLocationRef.value = it
                                }
                        layer.cameraPosition()?.let { pos ->
                            mapView.mapWindow.map.move(
                                pos,
                                com.yandex.mapkit.Animation(
                                    com.yandex.mapkit.Animation.Type.SMOOTH, 1.5f
                                ),
                                null
                            )
                        }
                    }
                } else {
                    locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            elevation      = FloatingActionButtonDefaults.elevation(6.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.MyLocation,
                contentDescription = "Моё местоположение",
                tint               = MaterialTheme.colorScheme.primary
            )
        }

        // Snackbar (снизу по центру)
        SnackbarHost(
            hostState = snackbar,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }

    // ── Bottom Sheet — карточка выбранного места ──────────────────────────
    state.selectedPlace?.let { place ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectPlace(null) },
            sheetState       = sheetState
        ) {
            PlaceBottomSheetContent(
                place        = place,
                onViewDetail = {
                    val id = place.id
                    viewModel.selectPlace(null)
                    onPlaceClick(id)
                }
            )
        }
    }
}

// ── Содержимое Bottom Sheet ───────────────────────────────────────────────────

@Composable
private fun PlaceBottomSheetContent(
    place:        Place,
    onViewDetail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        // Обложка
        place.coverUrl?.let { url ->
            AsyncImage(
                model              = url,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(16.dp))
        }

        // Название
        Text(
            text       = place.name,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis
        )

        // Бейдж категории
        if (!place.categoryName.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            val badgeColor = Color(CATEGORY_COLORS[place.categoryId] ?: DEFAULT_MARKER_COLOR)
            Surface(
                shape = MaterialTheme.shapes.small,
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text       = place.categoryName,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = badgeColor,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Адрес
        if (!place.address.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text     = place.address,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Рейтинг в виде звёзд
        if (place.averageRating > 0) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        imageVector        = if (i < place.averageRating.toInt())
                            Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = Color(0xFFFFB300)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "%.1f".format(place.averageRating),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = " · ${place.reviewsCount} отзывов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Краткое описание
        if (!place.description.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text     = place.description,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick  = onViewDetail,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Подробнее", fontWeight = FontWeight.SemiBold)
        }
    }
}
