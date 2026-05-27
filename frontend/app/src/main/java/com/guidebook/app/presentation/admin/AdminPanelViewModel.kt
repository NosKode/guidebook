package com.guidebook.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.usecase.admin.ApprovePlaceUseCase
import com.guidebook.app.domain.usecase.admin.GetPendingPlacesUseCase
import com.guidebook.app.domain.usecase.admin.RejectPlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val pendingPlaces: List<Place> = emptyList(),
    val isLoading:     Boolean     = false,
    val error:         String?     = null
)

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    private val getPendingPlaces: GetPendingPlacesUseCase,
    private val approvePlace:     ApprovePlaceUseCase,
    private val rejectPlace:      RejectPlaceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getPendingPlaces()) {
                is ApiResult.Success ->
                    _state.update { it.copy(pendingPlaces = result.data, isLoading = false) }
                is ApiResult.Error ->
                    _state.update { it.copy(isLoading = false, error = "Ошибка загрузки: ${result.code}") }
                is ApiResult.NetworkError ->
                    _state.update { it.copy(isLoading = false, error = "Нет подключения к сети") }
            }
        }
    }

    fun approve(placeId: String) {
        viewModelScope.launch {
            when (approvePlace(placeId)) {
                is ApiResult.Success ->
                    _state.update { it.copy(pendingPlaces = it.pendingPlaces.filter { p -> p.id != placeId }) }
                is ApiResult.Error ->
                    _state.update { it.copy(error = "Не удалось одобрить место") }
                is ApiResult.NetworkError ->
                    _state.update { it.copy(error = "Нет подключения к сети") }
            }
        }
    }

    fun reject(placeId: String, reason: String?) {
        viewModelScope.launch {
            when (rejectPlace(placeId, reason)) {
                is ApiResult.Success ->
                    _state.update { it.copy(pendingPlaces = it.pendingPlaces.filter { p -> p.id != placeId }) }
                is ApiResult.Error ->
                    _state.update { it.copy(error = "Не удалось отклонить место") }
                is ApiResult.NetworkError ->
                    _state.update { it.copy(error = "Нет подключения к сети") }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
