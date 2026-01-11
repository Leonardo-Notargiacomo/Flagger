package com.fontys.frontend.data.services

import com.fontys.frontend.data.models.Review
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewService {

    @POST("review")
    suspend fun postReview(@Body review: Review): Response<Review>

    @GET("reviews/{userId}")
    suspend fun getUserReviews(@Path("userId") userId: Int): Response<List<Review>>

    @GET("flag-review/{flagId}")
    suspend fun getFlagReviews(@Path("flagId") flagId: String?): Response<List<Review>>
}