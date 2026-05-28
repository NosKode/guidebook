package com.guidebook.app.presentation.catalog

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.guidebook.app.presentation.components.EmptyState
import com.guidebook.app.presentation.components.ErrorMessage
import com.guidebook.app.presentation.components.PlaceCard
import com.guidebook.app.presentation.components.ShimmerPlaceGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onPlaceClick: (String) -> Unit = {},
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    // ── Pull-to-refresh state живёт здесь, чтобы PullToRefreshContainer
    //    обёртывал ВЕСЬ контент (поиск + чипы + список) и не вылезал за их пределы.
    val pullToRefreshState = rememberPullToRefreshState()

    // Пользователь потянул → запускаем refresh
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) { viewModel.refresh() }
    }

    // ViewModel закончила обновлять → убираем индикатор PTR
    // Проверяем pullToRefreshState.isRefreshing, чтобы не вызывать endRefresh() вхолостую
    // при search/filter (которые тоже меняют state.isRefreshing, но не через жест PTR)
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Путеводитель",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // ── Кнопка сортировки ────────────────────────────────────
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Сортировка"
                            )
                        }
                        DropdownMenu(
                            expanded         = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text       = option.label,
                                            fontWeight = if (state.sortOption == option)
                                                FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (state.sortOption == option) {
                                        {
                                            Icon(
                                                imageVector        = Icons.Default.Check,
                                                contentDescription = null,
                                                tint               = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { scaffoldPadding ->

        // ── Внешний Box = граница клипа для PTR-индикатора ────────────────────
        // nestedScroll здесь → PullToRefreshContainer находится на ОДНОМ уровне
        // со всем контентом, не уходит за пределы экрана через Column.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Поиск ────────────────────────────────────────────────────
                TextField(
                    value         = state.searchQuery,
                    onValueChange = { viewModel.search(it) },
                    placeholder   = { Text("Поиск мест...") },
                    leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                // ── Категории ────────────────────────────────────────────────
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategoryId == null,
                            onClick  = { viewModel.filterByCategory(null) },
                            label    = { Text("Все") }
                        )
                    }
                    items(items = state.categories, key = { it.id }) { category ->
                        FilterChip(
                            selected = state.selectedCategoryId == category.id,
                            onClick  = { viewModel.filterByCategory(category.id) },
                            label    = { Text(category.name) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Контент ──────────────────────────────────────────────────
                when {
                    state.isLoading -> ShimmerPlaceGrid()
                    state.error != null && state.places.isEmpty() -> ErrorMessage(
                        message = state.error!!,
                        onRetry = { viewModel.refresh() }
                    )
                    state.places.isEmpty() -> EmptyState(
                        icon     = Icons.Outlined.SearchOff,
                        title    = "Ничего не найдено",
                        subtitle = "Попробуйте изменить запрос\nили выбрать другую категорию"
                    )
                    else -> PlacesGrid(
                        state        = state,
                        onPlaceClick = onPlaceClick,
                        onLoadMore   = { viewModel.loadNextPage() }
                    )
                }
            }

            // ── PTR-индикатор — теперь он ВНУТРИ того же Box что и весь контент.
            //    graphicsLayer { translationY = -height } прячет его ЗА TopAppBar,
            //    а не поверх категорий/поиска.
            PullToRefreshContainer(
                state    = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

// ── Сетка мест + пагинация (без PTR — он теперь выше) ───────────────────────

@Composable
private fun PlacesGrid(
    state:        CatalogUiState,
    onPlaceClick: (String) -> Unit,
    onLoadMore:   () -> Unit
) {
    val gridState = rememberLazyGridState()

    // Пагинация: триггер за 4 элемента до конца
    val shouldLoadMore by remember {
        derivedStateOf {
            val info         = gridState.layoutInfo
            val visibleItems = info.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf false
            val lastIndex  = visibleItems.last().index
            val totalItems = info.totalItemsCount
            lastIndex >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyVerticalGrid(
        columns             = GridCells.Fixed(1),
        state               = gridState,
        contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier            = Modifier.fillMaxSize()
    ) {
        items(
            items = state.places,
            key   = { it.id }
        ) { place ->
            PlaceCard(
                place   = place,
                onClick = { onPlaceClick(place.id) }
            )
        }

        // Индикатор загрузки следующей страницы
        if (state.isLoadingMore) {
            item(span = { GridItemSpan(1) }) {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}
