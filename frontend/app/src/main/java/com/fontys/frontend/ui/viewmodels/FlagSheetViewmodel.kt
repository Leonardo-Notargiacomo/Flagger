package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.models.Review
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

    private val reviewState = MutableStateFlow(ReviewState())
    val review: StateFlow<ReviewState> = reviewState.asStateFlow()

    private val reviewListState = MutableStateFlow(ReviewList())
    val reviewList: StateFlow<ReviewList> = reviewListState.asStateFlow()

    fun updateTitle(newTitle: String) {
        reviewState.update { reviewState -> reviewState.copy(title = newTitle) }
    }

    fun updateReview(newReview: String) {
        reviewState.update { reviewState -> reviewState.copy(review = newReview) }
    }

    fun updateRating(newRate: Double) {
        reviewState.update { reviewState -> reviewState.copy(rating = newRate) }
    }


    fun getFlagReviews()  {
        viewModelScope.launch {
            try {
                val reviews: List<Review>

            } catch (e: Exception) {
                throw Exception(e.message)
            }
        }
    }

    fun getUserReviews() {
        viewModelScope.launch {
            try {
                val reviews: List<Review>

            } catch (e: Exception) {
                throw Exception(e.message)
            }
        }
    }


    fun postReview(review: Review) {
        viewModelScope.launch {
            try {
                val review: ReviewState = reviewState.value

            } catch (e: Exception) {
                throw Exception(e.message)
            }
        }
    }

    init {
        loadSampleReviews()
    }

    private fun loadSampleReviews() {
        val sampleReviews = listOf(
            ReviewState(id = 1, title = "Amazing place!", review = "", rating = 5.0),
            ReviewState(id = 2, title = "Beautiful scenery", review = "", rating = 4.5),
            ReviewState(id = 3, title = "Nice spot", review = "", rating = 4.0),
            ReviewState(id = 4, title = "Hidden gem", review = "", rating = 4.8),
            ReviewState(id = 5, title = "Overrated", review = "", rating = 2.5),
            ReviewState(id = 6, title = "Must visit!", review = "", rating = 5.0)
        )

        reviewListState.update {
            ReviewList(reviews = sampleReviews)
        }
    }

}