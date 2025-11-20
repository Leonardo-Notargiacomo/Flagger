package com.fontys.frontend.data.models

data class Challenge(
    val id: Int,
    val name: String,
    val description: String,
    val targetValue: Int,
    val rewardPoints: Int,
    val badgeId: Int?,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = true
)

data class UserChallenge(
    val id: Int,
    val challenge: Challenge,
    val currentProgress: Int,
    val isCompleted: Boolean,
    val completedAt: String?,
    val startedAt: String
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

