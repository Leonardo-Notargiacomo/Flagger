package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class ReviewViewModel : ViewModel() {

    private val reviewState = MutableStateFlow(ReviewState())
    val review: StateFlow<ReviewState> = reviewState.asStateFlow()

    fun updateTitle(newTitle: String) {
        reviewState.update { reviewState -> reviewState.copy(title=newTitle) }
    }

    fun updateReview(newReview: String) {
        reviewState.update { reviewState -> reviewState.copy(review = newReview) }
    }

    fun updateRating(newRate: Double) {
        reviewState.update { reviewState -> reviewState.copy(rating = newRate) }
    }

    fun postReview() {
        viewModelScope.launch { ReviewRepository().postReview(reviewState) }
    }
}