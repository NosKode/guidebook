package com.guidebook.app.presentation.myplaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
        "Одобренные"  to state.approved.size,
        "На проверке" to state.pending.size,
        "Отклонённые" to state.rejected.size
    )

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick          = onAddPlace,
                icon             = { Icon(Icons.Default.Add, contentDescription = null) },
                text             = { Text("Добавить место", fontWeight = FontWeight.SemiBold) },
                containerColor   = MaterialTheme.colorScheme.primary,
                contentColor     = MaterialTheme.colorScheme.onPrimary,
                shape            = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Hero header ────────────────────────────────────────────────
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
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Place,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text       = "Мои места",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        val total = state.approved.size + state.pending.size + state.rejected.size
                        if (total > 0) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text  = "$total ${pluralPlaces(total)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Tabs ──────────────────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex  = selectedTab,
                edgePadding       = 16.dp,
                containerColor    = MaterialTheme.colorScheme.background,
                contentColor      = MaterialTheme.colorScheme.primary,
                indicator         = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color    = MaterialTheme.colorScheme.primary
                    )
                },
                divider           = {}
            ) {
                tabs.forEachIndexed { index, (label, count) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(
                                text       = if (count > 0) "$label ($count)" else label,
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold
                                             else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // ── Content ───────────────────────────────────────────────────
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = state.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(
                            onClick = viewModel::load,
                            colors  = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) { Text("Повторить") }
                    }
                }

                else -> when (selectedTab) {
                    0 -> PlacesList(
                        places       = state.approved,
                        emptyText    = "Одобренных мест пока нет",
                        onPlaceClick = onPlaceClick
                    )
                    1 -> PlacesList(
                        places       = state.pending,
                        emptyText    = "Нет мест на проверке",
                        onPlaceClick = onPlaceClick
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

private fun pluralPlaces(count: Int): String = when {
    count % 100 in 11..19 -> "мест"
    count % 10 == 1        -> "место"
    count % 10 in 2..4     -> "места"
    else                   -> "мест"
}

// ── Список мест ──────────────────────────────────────────────────────────────

@Composable
private fun PlacesList(
    places:       List<Place>,
    emptyText:    String,
    onPlaceClick: (String) -> Unit
) {
    if (places.isEmpty()) {
        EmptyTab(emptyText)
        return
    }
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(places, key = { it.id }) { place ->
            PlaceCard(
                place   = place,
                onClick = { onPlaceClick(place.id) }
            )
        }
    }
}

// ── Список отклонённых ───────────────────────────────────────────────────────

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
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(places, key = { it.id }) { place ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PlaceCard(
                    place           = place,
                    onClick         = { onPlaceClick(place.id) },
                    showStatusBadge = true
                )
                if (!place.rejectionReason.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text     = "Причина: ${place.rejectionReason}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
        modifier         = Modifier.fillMaxSize(),
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
                tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text      = text,
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
