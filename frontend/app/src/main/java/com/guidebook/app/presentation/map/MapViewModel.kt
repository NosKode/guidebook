package com.guidebook.app.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Category
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.CategoryRepository
import com.guidebook.app.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val allPlaces:          List<Place>    = emptyList(),
    val filteredPlaces:     List<Place>    = emptyList(),
    val categories:         List<Category> = emptyList(),
    val selectedCategoryId: Int?           = null,
    val selectedPlace:      Place?         = null,
    val isLoading:          Boolean        = false,
    val error:              String?        = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val placeRepository:    PlaceRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Загрузка категорий
            when (val cats = categoryRepository.getCategories()) {
                is ApiResult.Success -> _state.update { it.copy(categories = cats.data) }
                else -> {}
            }

            // Загрузка всех одобренных мест (большая страница)
            when (val result = placeRepository.getPlaces(page = 1, pageSize = 200)) {
                is ApiResult.Success -> {
                    val places = result.data.items
                    _state.update {
                        it.copy(
                            allPlaces      = places,
                            filteredPlaces = places,
                            isLoading      = false
                        )
                    }
                }
                is ApiResult.Error ->
                    _state.update { it.copy(isLoading = false, error = "Ошибка загрузки мест") }
                is ApiResult.NetworkError ->
                    _state.update { it.copy(isLoading = false, error = "Нет подключения к сети") }
            }
        }
    }

    fun filterByCategory(categoryId: Int?) {
        _state.update { s ->
            val filtered = if (categoryId == null)
                s.allPlaces
            else
                s.allPlaces.filter { it.categoryId == categoryId }
            s.copy(selectedCategoryId = categoryId, filteredPlaces = filtered)
        }
    }

    fun selectPlace(place: Place?) = _state.update { it.copy(selectedPlace = place) }

    fun clearError() = _state.update { it.copy(error = null) }
}
