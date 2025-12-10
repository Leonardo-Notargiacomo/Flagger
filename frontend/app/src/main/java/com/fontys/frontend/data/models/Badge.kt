package com.fontys.frontend.data.models
data class Badge(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val category: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: String? = null,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1
)

data class UserBadgesResponse(
    val badges: List<Badge>,
    val totalBadges: Int,
    val earnedBadges: Int
)