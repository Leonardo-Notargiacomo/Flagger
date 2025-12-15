package com.fontys.frontend.data.services

import com.fontys.frontend.data.models.Review
import retrofit2.http.GET
import retrofit2.http.POST

interface ReviewService {
    @POST("review")
    suspend fun postReview(): Review
    @GET("reviews")
    fun collectReviews(): Review
}