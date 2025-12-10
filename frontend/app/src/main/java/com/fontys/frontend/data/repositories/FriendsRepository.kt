package com.fontys.frontend.data.repositories

import android.util.Log
import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.remote.ApiClient
import retrofit2.Response
import org.json.JSONObject

class FriendsRepository {
    private val api = ApiClient.friendsApi

    companion object {
        private const val TAG = "FriendsRepository"
    }

    // User Operations
    suspend fun getUserById(token: String, userId: Int): Result<User> {
        Log.d(TAG, "getUserById() called with userId: $userId")
        return try {
            val response = api.getUserById("Bearer $token", userId)
            Log.d(TAG, "getUserById() response code: ${response.code()}")
            Log.d(TAG, "getUserById() response body: ${response.body()}")
            handleResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "getUserById() error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // User Search
    suspend fun searchUsers(token: String, query: String): Result<List<User>> {
        Log.d(TAG, "searchUsers() called with query: $query")
        return try {
            // Escape special regex characters to treat query as literal string
            // We cannot use Pattern.quote() because the backend (Node.js) doesn't support \Q...\E
            val escapedQuery = query.replace("\\", "\\\\")
                .replace(".", "\\.")
                .replace("*", "\\*")
                .replace("+", "\\+")
                .replace("?", "\\?")
                .replace("^", "\\^")
                .replace("$", "\\$")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("|", "\\|")

            val regexPattern = "/$escapedQuery/i"

            // Construct the filter object using JSONObject
            // Structure: {"where": {"or": [{"userName": {"regexp": ...}}, {"email": {"regexp": ...}}]}}
            val userNameClause = JSONObject().put("userName", JSONObject().put("regexp", regexPattern))
            val emailClause = JSONObject().put("email", JSONObject().put("regexp", regexPattern))
            
            val orArray = org.json.JSONArray()
                .put(userNameClause)
                .put(emailClause)

            val whereClause = JSONObject().put("or", orArray)
            val filterJson = JSONObject().put("where", whereClause).toString()

            val response = api.searchUsers("Bearer $token", filterJson)
            Log.d(TAG, "searchUsers() response code: ${response.code()}")
            handleResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "searchUsers() error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Friend Requests
    suspend fun sendFriendRequest(token: String, toUserId: Int): Result<FriendRequest> {
        Log.d(TAG, "sendFriendRequest() called with toUserId: $toUserId")
        return try {
            val response = api.sendFriendRequest(
                token = "Bearer $token",
                body = SendFriendRequestBody(toUserId)
            )
            Log.d(TAG, "sendFriendRequest() response code: ${response.code()}")
            Log.d(TAG, "sendFriendRequest() response body: ${response.body()}")
            handleResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "sendFriendRequest() error: ${e.message}", e)
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
        Log.d(TAG, "getSentRequests() called")
        return try {
            val response = api.getSentRequests("Bearer $token")
            Log.d(TAG, "getSentRequests() response code: ${response.code()}")
            Log.d(TAG, "getSentRequests() response body size: ${response.body()?.size}")
            response.body()?.forEachIndexed { index, request ->
                Log.d(TAG, "getSentRequests() request[$index]: id=${request.id}, toUserId=${request.toUserId}, status=${request.status}")
                Log.d(TAG, "getSentRequests() request[$index].toUser: ${request.toUser}")
                Log.d(TAG, "getSentRequests() request[$index].toUser.userName: ${request.toUser?.userName}")
            }
            handleResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "getSentRequests() error: ${e.message}", e)
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
        Log.d(TAG, "getFriends() called")
        return try {
            val response = api.getFriends("Bearer $token")
            Log.d(TAG, "getFriends() response code: ${response.code()}")
            Log.d(TAG, "getFriends() response body size: ${response.body()?.size}")
            response.body()?.forEachIndexed { index, friend ->
                Log.d(TAG, "getFriends() friend[$index]: friendId=${friend.friendId}, userName=${friend.friendDetails?.userName ?: "null"}")
            }
            handleResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "getFriends() error: ${e.message}", e)
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
