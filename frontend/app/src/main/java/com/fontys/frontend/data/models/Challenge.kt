package com.fontys.frontend.data.models

enum class ChallengeType {
    TIME_BASED,  // Challenges that need to be completed within 24 hours
    COUNT,       // Challenges that require a certain count (e.g., drink 8 glasses)
    STREAK       // Challenges that track consecutive days
}

data class Challenge(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val conditionType: String,
    val conditionParams: Map<String, Any>?,
    val rewardBadgeId: Int?,
    val cooldownHours: Int?,
    val expirationHours: Int?,
    val isActive: Boolean,
    val difficulty: String,
    val displayOrder: Int,
    val allowsExtension: Boolean?
) {
    // Helper to get the enum type
    fun getType(): ChallengeType {
        return when (conditionType.uppercase()) {
            "EXPLORATION_COUNT" -> ChallengeType.COUNT
            "STREAK" -> ChallengeType.STREAK
            else -> ChallengeType.TIME_BASED
        }
    }
}

data class UserChallenge(
    val id: Int,
    val challengeId: Int,
    val userId: Int,
    val status: String,
    val activatedAt: String?,
    val completedAt: String?,
    val expiresAt: String?,
    val cooldownEndsAt: String?,
    val progressData: Map<String, Any>?,
    val challenge: Challenge?
)

data class ChallengeStatusResponse(
    val hasActiveChallenge: Boolean,
    val activeChallenge: UserChallenge?,
    val isOnCooldown: Boolean,
    val cooldownEndsAt: String?,
    val availableChallenges: List<Challenge>?
)

data class ChallengeSelectionResponse(
    val userChallenge: UserChallenge,
    val challenge: Challenge,
    val cooldownEndsAt: String
)

data class ChallengeCompletionResult(
    val completed: Boolean,
    val badge: Badge?)

