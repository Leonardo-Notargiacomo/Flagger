package com.fontys.frontend.data.repositories

import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.remote.ApiClient
import retrofit2.Response

class FriendsRepository {
    private val api = ApiClient.friendsApi

    // Friend Requests
    suspend fun sendFriendRequest(token: String, toUserId: Int): Result<FriendRequest> {
        return try {
            val response = api.sendFriendRequest(
                token = "Bearer $token",
                body = SendFriendRequestBody(toUserId)
            )
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReceivedRequests(token: String): Result<List<FriendRequest>> {
        return try {
            val response = api.getReceivedRequests("Bearer $token")
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSentRequests(token: String): Result<List<FriendRequest>> {
        return try {
            val response = api.getSentRequests("Bearer $token")
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(token: String, requestId: Int): Result<AcceptFriendRequestResponse> {
        return try {
            val response = api.acceptFriendRequest("Bearer $token", requestId)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectFriendRequest(token: String, requestId: Int): Result<RejectFriendRequestResponse> {
        return try {
            val response = api.rejectFriendRequest("Bearer $token", requestId)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelFriendRequest(token: String, requestId: Int): Result<Unit> {
        return try {
            val response = api.cancelFriendRequest("Bearer $token", requestId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Friendships
    suspend fun getFriends(token: String): Result<List<FriendListItem>> {
        return try {
            val response = api.getFriends("Bearer $token")
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(token: String, friendId: Int): Result<Unit> {
        return try {
            val response = api.removeFriend("Bearer $token", friendId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendFlags(token: String, friendId: Int): Result<List<Flag>> {
        return try {
            val response = api.getFriendFlags("Bearer $token", friendId)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper function to handle responses
    private fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
        }
    }
}
