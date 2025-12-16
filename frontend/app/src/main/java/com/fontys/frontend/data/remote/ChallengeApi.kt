package com.fontys.frontend.data.remote

import com.fontys.frontend.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ChallengeApi {

    // GET /challenges - Get all challenges
    @GET("challenges")
    suspend fun getAllChallenges(): Response<List<Challenge>>

    // GET /challenges/active - Get active challenges
    @GET("challenges/active")
    suspend fun getActiveChallenges(): Response<List<Challenge>>

    // GET /challenges/available - Get available challenges
    @GET("challenges/available")
    suspend fun getAvailableChallenges(): Response<List<Challenge>>

    // GET /challenges/by-difficulty/{difficulty} - Get challenges by difficulty
    @GET("challenges/by-difficulty/{difficulty}")
    suspend fun getChallengesByDifficulty(
        @Path("difficulty") difficulty: String
    ): Response<List<Challenge>>

    // GET /challenges/count - Get challenge count
    @GET("challenges/count")
    suspend fun getChallengeCount(): Response<Int>

    // GET /challenges/status - Get challenge status
    @GET("challenges/status")
    suspend fun getChallengeStatus(): Response<ChallengeStatusResponse>

    // GET /challenges/history - Get challenge history
    @GET("challenges/history")
    suspend fun getChallengeHistory(): Response<List<UserChallenge>>

    // GET /challenges/{id} - Get specific challenge
    @GET("challenges/{id}")
    suspend fun getChallengeById(
        @Path("id") challengeId: Int
    ): Response<Challenge>

    // POST /challenges - Create new challenge
    @POST("challenges")
    suspend fun createChallenge(
        @Body challenge: Challenge
    ): Response<Challenge>

    // POST /challenges/{id}/select - Select a challenge
    @POST("challenges/{id}/select")
    suspend fun selectChallenge(
        @Path("id") challengeId: Int
    ): Response<ChallengeSelectionResponse>

    // POST /challenges/check-completion - Check challenge completion
    @POST("challenges/check-completion")
    suspend fun checkChallengeCompletion(): Response<ChallengeCompletionResult>

    // PATCH /challenges - Update challenges (batch)
    @PATCH("challenges")
    suspend fun updateChallenges(
        @Body challenges: List<Challenge>
    ): Response<List<Challenge>>

    // PATCH /challenges/{id} - Update specific challenge
    @PATCH("challenges/{id}")
    suspend fun updateChallenge(
        @Path("id") challengeId: Int,
        @Body challenge: Challenge
    ): Response<Challenge>

    // DELETE /challenges/{id} - Delete challenge
    @DELETE("challenges/{id}")
    suspend fun deleteChallenge(
        @Path("id") challengeId: Int
    ): Response<Void>
}
