package com.guidebook.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.remote.ApiResult
import com.guidebook.app.domain.model.Photo
import com.guidebook.app.domain.model.Place
import com.guidebook.app.domain.model.Review
import com.guidebook.app.domain.model.UserRole
import com.guidebook.app.domain.repository.AuthRepository
import com.guidebook.app.domain.repository.FavoriteRepository
import com.guidebook.app.domain.repository.PhotoRepository
import com.guidebook.app.domain.repository.PlaceRepository
import com.guidebook.app.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val place: Place? = null,
    val photos: List<Photo> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Текущий пользователь
    val currentUserId: String? = null,
    val isCurrentUserOwner: Boolean = false,
    val isAdmin: Boolean = false,
    // Отзыв текущего пользователя (null — ещё не оставлял)
    val currentUserReview: Review? = null,
    // Состояние переключения избранного
    val isTogglingFavorite: Boolean = false,
    // Состояние отправки отзыва
    val isSubmittingReview: Boolean = false,
    val reviewError: String? = null
)

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val placeRepository: PlaceRepository,
    private val photoRepository: PhotoRepository,
    private val reviewRepository: ReviewRepository,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // placeId берём из SavedStateHandle — Hilt/Navigation кладёт туда аргументы маршрута
    private val placeId: String = checkNotNull(savedStateHandle["placeId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    // ── Загрузка всех данных параллельно ────────────────────────────────────

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val placeDeferred     = async { placeRepository.getPlaceById(placeId) }
            val photosDeferred    = async { photoRepository.getPhotos(placeId) }
            val reviewsDeferred   = async { reviewRepository.getReviews(placeId) }
            val userDeferred      = async { authRepository.getMe() }
            val favoritesDeferred = async { favoriteRepository.getFavorites() }

            val placeResult     = placeDeferred.await()
            val photosResult    = photosDeferred.await()
            val reviewsResult   = reviewsDeferred.await()
            val userResult      = userDeferred.await()
            val favoritesResult = favoritesDeferred.await()

            val place     = (placeResult as? ApiResult.Success)?.data
            val photos    = (photosResult as? ApiResult.Success)?.data ?: emptyList()
            val reviews   = (reviewsResult as? ApiResult.Success)?.data ?: emptyList()
            val user      = (userResult as? ApiResult.Success)?.data
            val favorites = (favoritesResult as? ApiResult.Success)?.data ?: emptyList()

            val isFavorite        = favorites.any { it.id == placeId }
            val currentUserReview = reviews.find { it.userId == user?.id }
            val isOwner           = user != null && place?.uploadedBy == user.id
            val isAdmin           = user?.role == UserRole.ADMIN

            if (place == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error     = (placeResult as? ApiResult.Error)?.message ?: "Место не найдено"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading           = false,
                        place               = place,
                        photos              = photos,
                        reviews             = reviews,
                        isFavorite          = isFavorite,
                        currentUserReview   = currentUserReview,
                        isCurrentUserOwner  = isOwner,
                        isAdmin             = isAdmin,
                        currentUserId       = user?.id,
                        error               = null
                    )
                }
            }
        }
    }

    // ── Избранное ────────────────────────────────────────────────────────────

    fun toggleFavorite() {
        val state = _uiState.value
        if (state.isTogglingFavorite) return
        viewModelScope.launch {
            _uiState.update { it.copy(isTogglingFavorite = true) }
            val result = if (state.isFavorite) {
                favoriteRepository.removeFavorite(placeId)
            } else {
                favoriteRepository.addFavorite(placeId)
            }
            _uiState.update {
                it.copy(
                    isTogglingFavorite = false,
                    isFavorite         = if (result is ApiResult.Success) !state.isFavorite else state.isFavorite
                )
            }
        }
    }

    // ── Отзывы ───────────────────────────────────────────────────────────────

    fun createReview(rating: Int, comment: String?) {
        if (rating == 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, reviewError = null) }
            when (val result = reviewRepository.createReview(placeId, rating, comment?.takeIf { it.isNotBlank() })) {
                is ApiResult.Success -> {
                    val newReview = result.data
                    _uiState.update { state ->
                        state.copy(
                            isSubmittingReview = false,
                            reviews            = state.reviews + newReview,
                            currentUserReview  = newReview,
                            reviewError        = null
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingReview = false, reviewError = result.message)
                }
                is ApiResult.NetworkError -> _uiState.update {
                    it.copy(isSubmittingReview = false, reviewError = "Нет соединения")
                }
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            if (reviewRepository.deleteReview(reviewId) is ApiResult.Success) {
                _uiState.update { state ->
                    state.copy(
                        reviews           = state.reviews.filter { it.id != reviewId },
                        currentUserReview = if (state.currentUserReview?.id == reviewId) null
                                           else state.currentUserReview
                    )
                }
            }
        }
    }
}
