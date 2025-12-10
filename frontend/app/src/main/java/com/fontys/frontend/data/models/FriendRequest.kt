package com.fontys.frontend.data.models

import com.google.gson.annotations.SerializedName

data class FriendRequest(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("fromUserId")
    val fromUserId: Int,

    @SerializedName("toUserId")
    val toUserId: Int,

    @SerializedName("status")
    val status: String, // "PENDING", "ACCEPTED", "REJECTED"

    @SerializedName("createdAt")
    val createdAt: String?,

    // Relations from backend (if includeRelations: true)
    @SerializedName("fromUser")
    val fromUser: User? = null,

    @SerializedName("toUser")
    val toUser: User? = null
)

data class SendFriendRequestBody(
    @SerializedName("toUserId")
    val toUserId: Int
)

data class AcceptFriendRequestResponse(
    @SerializedName("message")
    val message: String
)

data class RejectFriendRequestResponse(
    @SerializedName("message")
    val message: String
)
