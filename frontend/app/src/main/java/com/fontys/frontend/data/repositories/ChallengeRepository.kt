package com.fontys.frontend.data.repositories

import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChallengeRepository {

    private val api = ApiClient.challengeApi

    suspend fun getChallengeStatus(): Result<ChallengeStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChallengeStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch challenge status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChallengeHistory(): Result<List<UserChallenge>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChallengeHistory()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch challenge history: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun selectChallenge(challengeId: Int): Result<ChallengeSelectionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.selectChallenge(challengeId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to select challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkChallengeCompletion(): Result<ChallengeCompletionResult> = withContext(Dispatchers.IO) {
        try {
            val response = api.checkChallengeCompletion()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to check challenge completion: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
