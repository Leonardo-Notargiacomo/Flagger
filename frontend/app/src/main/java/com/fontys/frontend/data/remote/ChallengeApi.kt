package com.fontys.frontend.data.remote

import com.fontys.frontend.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ChallengeApi {

    @GET("api/challenges")
    suspend fun getAllChallenges(): Response<List<Challenge>>

    @GET("api/challenges/active")
    suspend fun getActiveChallenges(): Response<List<Challenge>>

    @GET("api/users/{userId}/challenges")
    suspend fun getUserChallenges(
        @Path("userId") userId: Int
    ): Response<UserChallengesResponse>

    @POST("api/users/{userId}/challenges/{challengeId}/start")
    suspend fun startChallenge(
        @Path("userId") userId: Int,
        @Path("challengeId") challengeId: Int
    ): Response<UserChallenge>

    @PUT("api/users/{userId}/challenges/{challengeId}/progress")
    suspend fun updateChallengeProgress(
        @Path("userId") userId: Int,
        @Path("challengeId") challengeId: Int,
        @Body progressRequest: ChallengeProgressRequest
    ): Response<ChallengeCompletionResponse>

    @GET("api/users/{userId}/challenges/{challengeId}")
    suspend fun getChallengeProgress(
        @Path("userId") userId: Int,
        @Path("challengeId") challengeId: Int
    ): Response<UserChallenge>
}

