package com.fontys.frontend.data

data class UserUpdate(
    val id: Int?,
    val userName: String?,
    val userImage: Int?,
    val bio: String?,
    val email: String? = null
)
