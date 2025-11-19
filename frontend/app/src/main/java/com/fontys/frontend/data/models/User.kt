package com.fontys.frontend.data.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("email")
    val email: String?,

    @SerializedName("userName")
    val userName: String?,

    @SerializedName("bio")
    val bio: String?,

    @SerializedName("userImage")
    val userImage: String?
)
