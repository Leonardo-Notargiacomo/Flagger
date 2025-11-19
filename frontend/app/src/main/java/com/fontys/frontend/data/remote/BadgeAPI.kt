package com.fontys.frontend.data.remote

import com.fontys.frontend.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface BadgeApi {

    @GET("api/badges")
    suspend fun getAllBadges(): Response<List<Badge>>

    @GET("api/users/{userId}/badges")
    suspend fun getUserBadges(
        @Path("userId") userId: Int
    ): Response<UserBadgesResponse>

    @GET("api/users/{userId}/stats")
    suspend fun getUserStats(
        @Path("userId") userId: Int
    ): Response<UserStats>

    @POST("api/users/{userId}/explorations")
    suspend fun logExploration(
        @Path("userId") userId: Int,
        @Body event: ExplorationEvent
    ): Response<ExplorationResponse>

    
}