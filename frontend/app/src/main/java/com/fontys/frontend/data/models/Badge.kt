package com.fontys.frontend.data.models
data class Badge(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val category: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: String? = null
)

data class UserBadgesResponse(
    val badges: List<Badge>,
    val totalBadges: Int,
    val earnedBadges: Int
)