package com.fontys.frontend.data.repositories

import com.fontys.frontend.data.models.Review
import com.fontys.frontend.data.remote.ApiClient
import com.fontys.frontend.ui.viewmodels.ReviewState
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Response

class ReviewRepository {

    private val reviewService = ApiClient.reviewApi

    fun postReview(reviewState: MutableStateFlow<ReviewState>) {
        try {

        }
        catch (e: Exception) {
            throw e
        }
    }

    suspend fun getFlagReviews(): Response<List<Review>> {
        return reviewService.getFlagReviews()
    }
}