package com.guidebook.app.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.local.prefs.ThemePreferences
import com.guidebook.app.data.local.util.uriToFile
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.User
import com.guidebook.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user:              User?   = null,
    val isLoading:         Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val avatarError:       String? = null,
    val isDarkTheme:       Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themePreferences: ThemePreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadMe()
        viewModelScope.launch {
            themePreferences.isDarkTheme.collectLatest { isDark ->
                _state.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            themePreferences.setDarkTheme(!_state.value.isDarkTheme)
        }
    }

    private fun loadMe() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = authRepository.getMe()) {
                is ApiResult.Success ->
                    _state.update { it.copy(user = result.data, isLoading = false) }
                else ->
                    _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingAvatar = true, avatarError = null) }
            val file = uriToFile(context, uri)
            if (file == null) {
                _state.update { it.copy(isUploadingAvatar = false, avatarError = "Не удалось прочитать файл") }
                return@launch
            }
            when (val result = authRepository.uploadAvatar(file)) {
                is ApiResult.Success ->
                    _state.update { it.copy(user = result.data, isUploadingAvatar = false) }
                is ApiResult.Error ->
                    _state.update { it.copy(isUploadingAvatar = false, avatarError = "Ошибка загрузки") }
                is ApiResult.NetworkError ->
                    _state.update { it.copy(isUploadingAvatar = false, avatarError = "Нет соединения") }
            }
        }
    }

    fun clearAvatarError() {
        _state.update { it.copy(avatarError = null) }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}
