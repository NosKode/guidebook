package com.guidebook.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.User
import com.guidebook.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user:      User?   = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { loadMe() }

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

    /** Очищает токен, затем вызывает callback для навигации. */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()   // ← clearToken() в DataStore
            onComplete()
        }
    }
}
