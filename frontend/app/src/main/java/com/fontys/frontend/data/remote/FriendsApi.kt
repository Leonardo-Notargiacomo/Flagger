package com.fontys.frontend.data.remote

import com.fontys.frontend.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface FriendsApi {

    // User search endpoint
    @GET("go-users")
    suspend fun searchUsers(
        @Header("Authorization") token: String,
        @Query("filter") filter: String
    ): Response<List<User>>

    // Get user by ID
    @GET("go-users/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<User>

    // Friend Requests endpoints
    @POST("friend-requests")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Body body: SendFriendRequestBody
    ): Response<FriendRequest>

    @GET("friend-requests/received")
    suspend fun getReceivedRequests(
        @Header("Authorization") token: String
    ): Response<List<FriendRequest>>

    @GET("friend-requests/sent")
    suspend fun getSentRequests(
        @Header("Authorization") token: String
    ): Response<List<FriendRequest>>

    @PATCH("friend-requests/{id}/accept")
    suspend fun acceptFriendRequest(
        @Header("Authorization") token: String,
        @Path("id") requestId: Int
    ): Response<AcceptFriendRequestResponse>

    @PATCH("friend-requests/{id}/reject")
    suspend fun rejectFriendRequest(
        @Header("Authorization") token: String,
        @Path("id") requestId: Int
    ): Response<RejectFriendRequestResponse>

    @DELETE("friend-requests/{id}")
    suspend fun cancelFriendRequest(
        @Header("Authorization") token: String,
        @Path("id") requestId: Int
    ): Response<Unit>

    // Friendships endpoints
    @GET("friends")
    suspend fun getFriends(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<List<FriendListItem>>

    @DELETE("friends/{friendId}")
    suspend fun removeFriend(
        @Header("Authorization") token: String,
        @Path("friendId") friendId: Int
    ): Response<Unit>

    @GET("friends/{friendId}/flags")
    suspend fun getFriendFlags(
        @Header("Authorization") token: String,
        @Path("friendId") friendId: Int
    ): Response<List<Flag>>


}
