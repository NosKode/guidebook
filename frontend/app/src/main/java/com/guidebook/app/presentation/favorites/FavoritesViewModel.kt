package com.guidebook.app.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesSyncState(
    val isLoading : Boolean = false,
    val error     : String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    /** Реактивный список из Room — обновляется при любом изменении кеша. */
    val favorites: StateFlow<List<Place>> = favoriteRepository
        .favoritesFlow()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _syncState = MutableStateFlow(FavoritesSyncState())
    val syncState: StateFlow<FavoritesSyncState> = _syncState.asStateFlow()

    init {
        syncWithServer()
    }

    fun syncWithServer() {
        viewModelScope.launch {
            _syncState.update { it.copy(isLoading = true, error = null) }
            when (val result = favoriteRepository.getFavorites()) {
                is ApiResult.Success    -> _syncState.update { it.copy(isLoading = false) }
                is ApiResult.Error      -> _syncState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> _syncState.update {
                    it.copy(isLoading = false, error = "Нет соединения с сервером")
                }
            }
        }
    }

    fun removeFavorite(placeId: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(placeId)
        }
    }
}
