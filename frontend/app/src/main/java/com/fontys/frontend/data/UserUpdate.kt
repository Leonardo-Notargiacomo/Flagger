package com.fontys.frontend.data

data class UserUpdate(
    val id: String?,
    val userName: String?,
    val userImage: String?,
    val bio: String?,
    val email: String? = null,
    val password: String? = null
)
