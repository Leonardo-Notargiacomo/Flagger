package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.models.Review
import com.fontys.frontend.data.repositories.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ReviewState(
    val id: Int = 0,
    val title: String = "",
    val review: String = "",
    val rating: Double = 0.0
)

data class ReviewList(
    val reviews: List<ReviewState> = listOf()
)

class FlagSheetViewmodel : ViewModel() {

    private val reviewRepository = ReviewRepository()

    private val reviewState = MutableStateFlow(ReviewState())
    val review: StateFlow<ReviewState> = reviewState.asStateFlow()

    private val reviewListState = MutableStateFlow(ReviewList())
    val reviewList: StateFlow<ReviewList> = reviewListState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentFlagId = MutableStateFlow<String?>(null)



    fun updateTitle(newTitle: String) {
        reviewState.update { reviewState -> reviewState.copy(title = newTitle) }
    }

    fun updateReview(newReview: String) {
        reviewState.update { reviewState -> reviewState.copy(review = newReview) }
    }

    fun updateRating(newRate: Double) {
        reviewState.update { reviewState -> reviewState.copy(rating = newRate) }
    }

    fun setCurrentFlag(flagId: String) {
        _currentFlagId.value = flagId
        getFlagReviews(flagId)
    }
    fun getFlagReviews(flagId: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = reviewRepository.getFlagReviews(flagId)

                if (response.isSuccessful && response.body() != null) {
                    val reviews = response.body()!!.map { review ->
                        ReviewState(
                            id = review.id,
                            title = review.title,
                            review = review.desc,
                            rating = review.rating ?: 0.0
                        )
                    }
                    reviewListState.value = ReviewList(reviews = reviews)
                } else {
                    _error.value = "Failed to load reviews: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load reviews"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun postReview(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = reviewRepository.postReview(createReviewFromState())
                if (response.isSuccessful) {
                    reviewState.value = ReviewState()
                    _currentFlagId.value?.let { flagId ->
                        getFlagReviews(flagId)
                    }
                    onSuccess()
                } else {
                    _error.value = "Failed to post review: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to post review"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun createReviewFromState(): Review {
        val currentReview = reviewState.value
        return Review(
            id = currentReview.id,
            title = currentReview.title,
            desc = currentReview.review,
            rating = currentReview.rating
        )
    }
}