package com.fontys.frontend.domain.model

/**
 * User profile data model for the account screen
 */
data class UserProfile(
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val profileImageUrl: String? = null
)
