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
    val targetValue: Int,
    val rewardPoints: Int,
    val badgeId: Int?,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = true,
    val challengeType: String? = "TIME_BASED" // Backend sends as string, can be null
) {
    // Helper to get the enum type
    fun getType(): ChallengeType {
        return when (challengeType?.uppercase()) {
            "COUNT" -> ChallengeType.COUNT
            "STREAK" -> ChallengeType.STREAK
            else -> ChallengeType.TIME_BASED
        }
    }
}

data class UserChallenge(
    val id: Int,
    val challenge: Challenge,
    val currentProgress: Int,
    val isCompleted: Boolean,
    val completedAt: String?,
    val startedAt: String? // Backend can return null for this field
)

data class ChallengeProgress(
    val challengeId: Int,
    val progress: Int,
    val isCompleted: Boolean
)

data class UserChallengesResponse(
    val activeChallenges: List<UserChallenge>,
    val completedChallenges: List<UserChallenge>,
    val totalChallenges: Int,
    val completedCount: Int
)

data class ChallengeProgressRequest(
    val progress: Int
)

data class ChallengeCompletionResponse(
    val success: Boolean,
    val challenge: UserChallenge,
    val newBadge: Badge?,
    val pointsAwarded: Int
)

