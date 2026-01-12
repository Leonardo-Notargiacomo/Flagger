package com.fontys.frontend.data.repositories

import com.fontys.frontend.data.models.Review
import com.fontys.frontend.data.remote.ApiClient
import retrofit2.Response

class ReviewRepository {

    private val reviewService = ApiClient.reviewApi

    suspend fun postReview(flagId: Int, review: Review): Response<Review> {
        return reviewService.postReview(flagId, review)
    }

    suspend fun getFlagReviews(flagId: Int): Response<List<Review>> {
        return reviewService.getFlagReviews(flagId)
    }
}