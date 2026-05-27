package com.guidebook.app.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Category
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.CategoryRepository
import com.guidebook.app.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Варианты сортировки ──────────────────────────────────────────────────────

enum class SortOption(val label: String, val apiValue: String?) {
    NEWEST("Новые",           null),
    NAME_AZ("По названию А-Я", "name")
}

// ── UI State ─────────────────────────────────────────────────────────────────

data class CatalogUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val places: List<Place> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Int? = null,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.NEWEST,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val error: String? = null
)

// ── ViewModel ────────────────────────────────────────────────────────────────

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    // Отдельный поток для поисковой строки.
    // MutableStateFlow испускает начальное значение ("") немедленно — используем
    // .drop(1) в observeSearch(), чтобы не дублировать начальную загрузку из init.
    private val _searchFlow = MutableStateFlow("")

    // Текущая задача загрузки — отменяем перед каждым новым запросом, чтобы избежать
    // гонки: результаты устаревшего запроса не перезапишут актуальные данные.
    private var loadJob: Job? = null

    init {
        loadCategories()
        loadPlaces(refresh = true)
        observeSearch()
    }

    // ── Публичные методы ────────────────────────────────────────────────────

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchFlow.value = query
    }

    fun filterByCategory(categoryId: Int?) {
        if (_uiState.value.selectedCategoryId == categoryId) return
        _uiState.update { it.copy(selectedCategoryId = categoryId, currentPage = 1, places = emptyList()) }
        loadPlaces(refresh = true)
    }

    fun setSortOption(option: SortOption) {
        if (_uiState.value.sortOption == option) return
        _uiState.update { it.copy(sortOption = option, currentPage = 1, places = emptyList()) }
        loadPlaces(refresh = true)
    }

    fun loadNextPage() {
        val state = _uiState.value
        // Не грузим, если уже идёт какая-либо загрузка или страниц больше нет
        if (state.isLoadingMore || state.isLoading || state.isRefreshing
            || state.currentPage >= state.totalPages) return
        loadPlaces(refresh = false)
    }

    fun refresh() {
        loadPlaces(refresh = true)
    }

    // ── Приватные методы ────────────────────────────────────────────────────

    private fun observeSearch() {
        viewModelScope.launch {
            _searchFlow
                .drop(1)             // Пропускаем начальное "" — init уже запустил загрузку
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    _uiState.update { it.copy(currentPage = 1, places = emptyList()) }
                    loadPlaces(refresh = true, query = query)
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = categoryRepository.getCategories()) {
                is ApiResult.Success      -> _uiState.update { it.copy(categories = result.data) }
                is ApiResult.Error        -> { /* некритично */ }
                is ApiResult.NetworkError -> { /* некритично */ }
            }
        }
    }

    fun loadPlaces(refresh: Boolean = false, query: String? = null) {
        val state = _uiState.value
        val effectiveQuery = query ?: state.searchQuery

        // Отменяем предыдущий запрос, чтобы не было гонок результатов
        loadJob?.cancel()

        if (refresh) {
            _uiState.update {
                it.copy(
                    isLoading     = it.places.isEmpty(),   // полноэкранный спиннер только при пустом списке
                    isRefreshing  = it.places.isNotEmpty(), // PTR-индикатор при уже загруженных данных
                    isLoadingMore = false,                  // сбрасываем на случай отмены пагинации
                    currentPage   = 1,
                    error         = null
                )
            }
        } else {
            _uiState.update { it.copy(isLoadingMore = true) }
        }

        val page = if (refresh) 1 else state.currentPage + 1

        loadJob = viewModelScope.launch {
            val result = placeRepository.getPlaces(
                page      = page,
                pageSize  = 20,
                categoryId = _uiState.value.selectedCategoryId,
                search    = effectiveQuery.takeIf { it.isNotBlank() },
                sortBy    = _uiState.value.sortOption.apiValue
            )

            when (result) {
                is ApiResult.Success -> {
                    val newPlaces = result.data.items
                    _uiState.update { current ->
                        current.copy(
                            isLoading     = false,
                            isRefreshing  = false,
                            isLoadingMore = false,
                            places        = if (refresh) newPlaces else current.places + newPlaces,
                            currentPage   = result.data.page,
                            totalPages    = result.data.totalPages,
                            error         = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading     = false,
                            isRefreshing  = false,
                            isLoadingMore = false,
                            error         = result.message
                        )
                    }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading     = false,
                            isRefreshing  = false,
                            isLoadingMore = false,
                            error         = "Нет подключения к интернету"
                        )
                    }
                }
            }
        }
    }
}
