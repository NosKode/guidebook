package com.guidebook.app.presentation.map

import android.graphics.PointF
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map as YandexMap
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoordinatePickerScreen(
    onBack:    () -> Unit,
    onConfirm: (lat: Double, lon: Double) -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density        = LocalDensity.current

    val pinWidthPx = with(density) { 40.dp.toPx().toInt() }

    // Состояние выбранной точки (через MutableState — InputListener читает .value)
    val pickedLatRef       = remember { mutableStateOf<Double?>(null) }
    val pickedLonRef       = remember { mutableStateOf<Double?>(null) }
    val pickedPlacemarkRef = remember { mutableStateOf<PlacemarkMapObject?>(null) }

    val pickedLat = pickedLatRef.value
    val pickedLon = pickedLonRef.value

    val mapView = remember { MapView(context) }

    // Стиль пина: якорь на кончике хвостика
    val pinStyle = remember {
        IconStyle().apply { setAnchor(PointF(0.5f, 1.0f)) }
    }

    // InputListener — регистрирует тап по карте
    val inputListener = remember {
        object : InputListener {
            override fun onMapTap(map: YandexMap, point: Point) {
                pickedLatRef.value = point.latitude
                pickedLonRef.value = point.longitude

                // Красный пин-маркер выбранной точки
                val bmp = createPinBitmap(0xFFE53935.toInt(), pinWidthPx)
                pickedPlacemarkRef.value?.let { map.mapObjects.remove(it) }
                pickedPlacemarkRef.value = map.mapObjects.addPlacemark().apply {
                    geometry = point
                    setIcon(ImageProvider.fromBitmap(bmp), pinStyle)
                }
            }

            override fun onMapLongTap(map: YandexMap, point: Point) {}
        }
    }

    // Жизненный цикл MapKit + cleanup
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_STOP  -> mapView.onStop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            runCatching { mapView.mapWindow.map.removeInputListener(inputListener) }
        }
    }

    // Инициализация карты и подключение InputListener
    LaunchedEffect(Unit) {
        mapView.mapWindow.map.move(
            CameraPosition(Point(55.7522, 37.6156), 12f, 0f, 0f)
        )
        mapView.mapWindow.map.addInputListener(inputListener)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбрать на карте", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (pickedLat != null && pickedLon != null) {
                        IconButton(onClick = { onConfirm(pickedLat, pickedLon) }) {
                            Icon(
                                imageVector        = Icons.Default.Check,
                                contentDescription = "Подтвердить",
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Карта (полный экран)
            AndroidView(
                factory  = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            // Подсказка «нажмите на карту» (пока точка не выбрана)
            if (pickedLat == null) {
                Surface(
                    modifier        = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 40.dp),
                    shape           = RoundedCornerShape(14.dp),
                    color           = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation  = 4.dp,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.TouchApp,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text      = "Нажмите на карту, чтобы выбрать точку",
                            style     = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                            color     = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Панель координат + кнопка подтверждения (снизу)
            if (pickedLat != null && pickedLon != null) {
                Surface(
                    modifier        = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(12.dp),
                    shape           = RoundedCornerShape(16.dp),
                    color           = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation  = 4.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = "Широта",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text       = "%.6f".format(pickedLat),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text  = "Долгота",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text       = "%.6f".format(pickedLon),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(onClick = { onConfirm(pickedLat, pickedLon) }) {
                            Icon(
                                imageVector        = Icons.Default.Check,
                                contentDescription = null,
                                modifier           = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Выбрать", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
