package com.fontys.frontend.data

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class UserLogin(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password : String
)

data class LoginResponse(
    @SerializedName("token")
    val token: String
)