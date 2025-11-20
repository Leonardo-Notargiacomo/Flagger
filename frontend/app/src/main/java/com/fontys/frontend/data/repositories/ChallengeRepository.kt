package com.fontys.frontend.data.repositories

import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChallengeRepository {

    private val api = ApiClient.challengeApi

    // GET /challenges - Get all challenges
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

    // GET /challenges/active - Get active challenges
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

    // GET /challenges/available - Get available challenges
    suspend fun getAvailableChallenges(): Result<List<Challenge>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAvailableChallenges()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch available challenges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // GET /challenges/by-difficulty/{difficulty} - Get challenges by difficulty
    suspend fun getChallengesByDifficulty(difficulty: String): Result<List<Challenge>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChallengesByDifficulty(difficulty)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch challenges by difficulty: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // GET /challenges/count - Get challenge count
    suspend fun getChallengeCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChallengeCount()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch challenge count: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // GET /challenges/history - Get challenge history
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

    // GET /challenges/status - Get challenge status
    suspend fun getChallengeStatus(): Result<Any> = withContext(Dispatchers.IO) {
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

    // GET /challenges/{id} - Get specific challenge
    suspend fun getChallengeById(challengeId: Int): Result<Challenge> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChallengeById(challengeId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // POST /challenges - Create new challenge
    suspend fun createChallenge(challenge: Challenge): Result<Challenge> = withContext(Dispatchers.IO) {
        try {
            val response = api.createChallenge(challenge)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // POST /challenges/{id}/select - Select a challenge
    suspend fun selectChallenge(challengeId: Int): Result<UserChallenge> = withContext(Dispatchers.IO) {
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

    // POST /challenges/check-completion - Check challenge completion
    suspend fun checkChallengeCompletion(completionRequest: Any): Result<ChallengeCompletionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.checkChallengeCompletion(completionRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to check challenge completion: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PATCH /challenges - Update challenges (batch)
    suspend fun updateChallenges(challenges: List<Challenge>): Result<List<Challenge>> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateChallenges(challenges)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update challenges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PATCH /challenges/{id} - Update specific challenge
    suspend fun updateChallenge(challengeId: Int, challenge: Challenge): Result<Challenge> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateChallenge(challengeId, challenge)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // DELETE /challenges/{id} - Delete challenge
    suspend fun deleteChallenge(challengeId: Int): Result<Void?> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteChallenge(challengeId)
            if (response.isSuccessful) {
                Result.success(null)
            } else {
                Result.failure(Exception("Failed to delete challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

