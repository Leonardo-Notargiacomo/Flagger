package com.fontys.frontend.data

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class UserRegister(
    @SerializedName("userName")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("bio")
    val bio: String,

    @SerializedName("password")
    val password : String
)