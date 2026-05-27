package com.guidebook.app.presentation.myplaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.model.PlaceStatus
import com.guidebook.app.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyPlacesUiState(
    val approved: List<Place>  = emptyList(),
    val pending:  List<Place>  = emptyList(),
    val rejected: List<Place>  = emptyList(),
    val isLoading: Boolean     = false,
    val error: String?         = null
)

@HiltViewModel
class MyPlacesViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyPlacesUiState())
    val state: StateFlow<MyPlacesUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = placeRepository.getMyPlaces()) {
                is ApiResult.Success -> {
                    val places = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            approved  = places.filter { p -> p.status == PlaceStatus.APPROVED },
                            pending   = places.filter { p -> p.status == PlaceStatus.PENDING  },
                            rejected  = places.filter { p -> p.status == PlaceStatus.REJECTED }
                        )
                    }
                }
                is ApiResult.Error ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.NetworkError ->
                    _state.update { it.copy(isLoading = false, error = "Нет подключения к сети") }
            }
        }
    }
}
