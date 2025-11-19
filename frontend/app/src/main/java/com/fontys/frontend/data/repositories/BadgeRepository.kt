package com.fontys.frontend.data.repositories

import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BadgeRepository {

    private val api = ApiClient.badgeApi

    suspend fun getAllBadges(): Result<List<Badge>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllBadges()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch badges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserBadges(userId: Int): Result<UserBadgesResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserBadges(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch user badges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserStats(userId: Int): Result<UserStats> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserStats(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch stats: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logExploration(
        userId: Int,
        event: ExplorationEvent
    ): Result<ExplorationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.logExploration(userId, event)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to log exploration: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}