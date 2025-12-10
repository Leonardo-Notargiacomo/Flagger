package com.fontys.frontend.data.models

import com.google.gson.annotations.SerializedName

data class FriendListItem(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("friendId")
    val friendId: Int,

    @SerializedName("friendDetails")
    val friendDetails: User? = null
)
