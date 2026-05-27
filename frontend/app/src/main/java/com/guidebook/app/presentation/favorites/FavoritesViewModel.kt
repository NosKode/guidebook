package com.guidebook.app.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    /** Реактивный список из Room — обновляется при любом изменении кеша. */
    val favorites: StateFlow<List<Place>> = favoriteRepository
        .favoritesFlow()
        .stateIn(
            scope           = viewModelScope,
            started         = SharingStarted.WhileSubscribed(5_000),
            initialValue    = emptyList()
        )

    init {
        // При первом открытии синхронизируем кеш с сервером
        syncWithServer()
    }

    private fun syncWithServer() {
        viewModelScope.launch {
            favoriteRepository.getFavorites()
        }
    }

    fun removeFavorite(placeId: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(placeId)
        }
    }
}
