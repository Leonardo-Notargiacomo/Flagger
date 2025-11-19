package com.fontys.frontend.data.repositories

import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChallengeRepository {

    private val api = ApiClient.challengeApi

    suspend fun getAllChallenges(): Result<List<Challenge>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllChallenges()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch challenges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveChallenges(): Result<List<Challenge>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getActiveChallenges()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch active challenges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserChallenges(userId: Int): Result<UserChallengesResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserChallenges(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch user challenges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startChallenge(userId: Int, challengeId: Int): Result<UserChallenge> = withContext(Dispatchers.IO) {
        try {
            val response = api.startChallenge(userId, challengeId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to start challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChallengeProgress(
        userId: Int,
        challengeId: Int,
        progress: Int
    ): Result<ChallengeCompletionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateChallengeProgress(
                userId,
                challengeId,
                ChallengeProgressRequest(progress)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update challenge progress: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChallengeProgress(userId: Int, challengeId: Int): Result<UserChallenge> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChallengeProgress(userId, challengeId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch challenge progress: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

