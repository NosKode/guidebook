package com.guidebook.app.presentation.myplaces

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.local.util.uriToFile
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPhotoUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error:     String? = null
)

@HiltViewModel
class AddPhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val placeId: String = checkNotNull(savedStateHandle["placeId"])

    private val _state = MutableStateFlow(AddPhotoUiState())
    val state: StateFlow<AddPhotoUiState> = _state.asStateFlow()

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    fun pickImage(uri: Uri) {
        selectedImageUri = uri
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun upload(caption: String?) {
        val uri = selectedImageUri
        if (uri == null) {
            _state.update { it.copy(error = "Выберите фотографию") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val file = uriToFile(context, uri)
            if (file == null) {
                _state.update { it.copy(isLoading = false, error = "Не удалось прочитать файл") }
                return@launch
            }
            when (val result = photoRepository.uploadPhoto(
                placeId = placeId,
                file    = file,
                caption = caption?.takeIf { it.isNotBlank() }
            )) {
                is ApiResult.Success ->
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                is ApiResult.Error ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.NetworkError ->
                    _state.update { it.copy(isLoading = false, error = "Нет подключения к сети") }
            }
            file.delete()
        }
    }
}
