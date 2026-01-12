package com.fontys.frontend.data.services

import com.fontys.frontend.data.models.Review
import com.fontys.frontend.data.models.ReviewPost
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewService {

    @POST("flags/{flagId}/reviews")
    suspend fun postReview(@Path("flagId") flagId: Int,@Body review: ReviewPost): Response<Review>

    @GET("reviews/{userId}")
    suspend fun getUserReviews(@Path("userId") userId: Int): Response<List<Review>>

    @GET("flags/{flagId}/reviews")
    suspend fun getFlagReviews(@Path("flagId") flagId: Int): Response<List<Review>>
}