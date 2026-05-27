package com.guidebook.app.presentation.myplaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.guidebook.app.domain.model.Place
import com.guidebook.app.presentation.components.PlaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlacesScreen(
    onAddPlace:   () -> Unit       = {},
    onPlaceClick: (String) -> Unit = {},
    onAddPhoto:   (String) -> Unit = {},
    viewModel: MyPlacesViewModel   = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Одобренные"    to state.approved.size,
        "На проверке"   to state.pending.size,
        "Отклонённые"   to state.rejected.size
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Мои места",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPlace,
                icon    = { Icon(Icons.Default.Add, contentDescription = null) },
                text    = { Text("Добавить место") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Вкладки ──────────────────────────────────────────────────
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, (label, count) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(
                                text = if (count > 0) "$label ($count)" else label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            // ── Контент вкладки ──────────────────────────────────────────
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.error != null -> ErrorState(
                    message  = state.error!!,
                    onRetry  = viewModel::load
                )

                else -> when (selectedTab) {
                    0 -> PlacesList(
                        places       = state.approved,
                        emptyText    = "Одобренных мест пока нет",
                        onPlaceClick = onPlaceClick,
                        onAddPhoto   = onAddPhoto
                    )
                    1 -> PlacesList(
                        places       = state.pending,
                        emptyText    = "Нет мест на проверке",
                        onPlaceClick = onPlaceClick,
                        onAddPhoto   = null
                    )
                    2 -> RejectedList(
                        places       = state.rejected,
                        onPlaceClick = onPlaceClick
                    )
                }
            }
        }
    }
}

// ── Список мест ──────────────────────────────────────────────────────────────

@Composable
private fun PlacesList(
    places:       List<Place>,
    emptyText:    String,
    onPlaceClick: (String) -> Unit,
    onAddPhoto:   ((String) -> Unit)?
) {
    if (places.isEmpty()) {
        EmptyTab(emptyText)
        return
    }
    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(places, key = { it.id }) { place ->
            PlaceCard(
                place   = place,
                onClick = { onPlaceClick(place.id) }
            )
        }
    }
}

// ── Список отклонённых (с причиной) ─────────────────────────────────────────

@Composable
private fun RejectedList(
    places:       List<Place>,
    onPlaceClick: (String) -> Unit
) {
    if (places.isEmpty()) {
        EmptyTab("Отклонённых мест нет")
        return
    }
    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(places, key = { it.id }) { place ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PlaceCard(
                    place           = place,
                    onClick         = { onPlaceClick(place.id) },
                    showStatusBadge = true
                )
                if (!place.rejectionReason.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(8.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text     = "Причина: ${place.rejectionReason}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

// ── Пустое состояние вкладки ─────────────────────────────────────────────────

@Composable
private fun EmptyTab(text: String) {
    Box(
        modifier        = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.Place,
                contentDescription = null,
                modifier           = Modifier.size(56.dp),
                tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text      = text,
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Состояние ошибки ─────────────────────────────────────────────────────────

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.Button(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}
