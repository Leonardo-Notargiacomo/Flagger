package com.fontys.frontend.data.models

data class ExplorationEvent(
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String? = null
)

data class ExplorationResponse(
    val success: Boolean,
    val event: ExplorationEvent,
    val streak: StreakInfo,
    val newBadges: List<Badge>
)

data class StreakInfo(
    val current: Int,
    val longest: Int
)