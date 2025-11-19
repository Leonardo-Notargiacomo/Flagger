package com.fontys.frontend.data.models

data class UserStats(
    val totalExplorations: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val badges: List<BadgeInfo>? = null
)

data class BadgeInfo(
    val id: Int,
    val name: String?,
    val description: String?,
    val unlockedAt: String?
)