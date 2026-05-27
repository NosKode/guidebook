package com.guidebook.app.presentation.myplaces

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.local.util.uriToFile
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Category
import com.guidebook.app.domain.repository.CategoryRepository
import com.guidebook.app.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPlaceUiState(
    val categories:        List<Category> = emptyList(),
    val categoriesLoading: Boolean        = false,
    val isLoading:         Boolean        = false,
    val isSuccess:         Boolean        = false,
    val error:             String?        = null
)

@HiltViewModel
class AddPlaceViewModel @Inject constructor(
    private val placeRepository:    PlaceRepository,
    private val categoryRepository: CategoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AddPlaceUiState())
    val state: StateFlow<AddPlaceUiState> = _state.asStateFlow()

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    init { loadCategories() }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(categoriesLoading = true) }
            when (val result = categoryRepository.getCategories()) {
                is ApiResult.Success ->
                    _state.update { it.copy(categories = result.data, categoriesLoading = false) }
                else ->
                    _state.update { it.copy(categoriesLoading = false) }
            }
        }
    }

    fun pickImage(uri: Uri) {
        selectedImageUri = uri
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun submit(
        name:        String,
        address:     String?,
        latitude:    Double?,
        longitude:   Double?,
        categoryId:  Int?,
        description: String?
    ) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "Название обязательно") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = placeRepository.addPlace(
                name, address?.takeIf { it.isNotBlank() },
                latitude, longitude, categoryId,
                description?.takeIf { it.isNotBlank() }
            )) {
                is ApiResult.Success -> {
                    val place = result.data
                    // Загружаем обложку если выбрана
                    val uri = selectedImageUri
                    if (uri != null) {
                        val file = uriToFile(context, uri)
                        if (file != null) {
                            placeRepository.uploadCover(place.id, file)
                            file.delete()
                        }
                    }
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is ApiResult.Error ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.NetworkError ->
                    _state.update { it.copy(isLoading = false, error = "Нет подключения к сети") }
            }
        }
    }
}
