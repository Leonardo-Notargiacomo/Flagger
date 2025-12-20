package com.fontys.frontend.data.services

import com.fontys.frontend.data.models.Review
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface ReviewService {

    @POST("review")
    suspend fun postReview(): Review

    @GET("/reviews/{id}")
    suspend fun getUserReviews(): Response<List<Review>>

    @GET("/flag-review/{id}")
    suspend fun getFlagReviews(): Response<List<Review>>
}