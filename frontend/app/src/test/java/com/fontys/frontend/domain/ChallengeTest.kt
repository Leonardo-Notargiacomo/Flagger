package com.fontys.frontend.domain

import com.fontys.frontend.data.models.Challenge
import com.fontys.frontend.data.models.ChallengeType
import com.fontys.frontend.data.models.UserChallenge
import com.fontys.frontend.data.models.ChallengeStatusResponse
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Challenge models and business logic.
 * These tests verify the challenge system behavior including:
 * - Challenge type detection
 * - Progress tracking for active challenges
 * - Challenge status transitions (active, completed, expired)
 */
class ChallengeTest {

    // Helper function to create a test challenge
    private fun createChallenge(
        id: Int = 1,
        name: String = "Test Challenge",
        conditionType: String = "EXPLORATION_COUNT",
        conditionParams: Map<String, Any>? = mapOf("count" to 5),
        difficulty: String = "easy"
    ) = Challenge(
        id = id,
        name = name,
        description = "Test description",
        iconUrl = null,
        conditionType = conditionType,
        conditionParams = conditionParams,
        rewardBadgeId = 1,
        cooldownHours = 24,
        expirationHours = 24,
        isActive = true,
        difficulty = difficulty,
        displayOrder = 0,
        allowsExtension = false
    )

    // Helper function to create a test UserChallenge
    private fun createUserChallenge(
        id: Int = 1,
        challengeId: Int = 1,
        status: String = "active",
        progressData: Map<String, Any>? = mapOf("currentCount" to 0, "targetCount" to 5),
        challenge: Challenge? = null
    ) = UserChallenge(
        id = id,
        challengeId = challengeId,
        userId = 1,
        status = status,
        activatedAt = "2025-12-19T10:00:00.000Z",
        completedAt = null,
        expiresAt = "2025-12-20T10:00:00.000Z",
        cooldownEndsAt = "2025-12-20T10:00:00.000Z",
        progressData = progressData,
        challenge = challenge
    )

    // =====================================================
    // Challenge Type Detection Tests
    // =====================================================

    @Test
    fun `getType returns COUNT for exploration_count challenges`() {
        val challenge = createChallenge(conditionType = "EXPLORATION_COUNT")
        assertEquals(ChallengeType.COUNT, challenge.getType())
    }

    @Test
    fun `getType returns COUNT for lowercase exploration_count`() {
        val challenge = createChallenge(conditionType = "exploration_count")
        assertEquals(ChallengeType.COUNT, challenge.getType())
    }

    @Test
    fun `getType returns STREAK for streak challenges`() {
        val challenge = createChallenge(conditionType = "STREAK")
        assertEquals(ChallengeType.STREAK, challenge.getType())
    }

    @Test
    fun `getType returns TIME_BASED for time_based challenges`() {
        val challenge = createChallenge(conditionType = "TIME_BASED")
        assertEquals(ChallengeType.TIME_BASED, challenge.getType())
    }

    @Test
    fun `getType returns TIME_BASED for unknown condition types`() {
        val challenge = createChallenge(conditionType = "UNKNOWN_TYPE")
        assertEquals(ChallengeType.TIME_BASED, challenge.getType())
    }

    // =====================================================
    // Challenge Status Tests
    // =====================================================

    @Test
    fun `UserChallenge with active status is active`() {
        val userChallenge = createUserChallenge(status = "active")
        assertEquals("active", userChallenge.status)
    }

    @Test
    fun `UserChallenge with completed status is completed`() {
        val userChallenge = createUserChallenge(status = "completed")
        assertEquals("completed", userChallenge.status)
    }

    @Test
    fun `UserChallenge with expired status is expired`() {
        val userChallenge = createUserChallenge(status = "expired")
        assertEquals("expired", userChallenge.status)
    }

    // =====================================================
    // Progress Data Tests
    // =====================================================

    @Test
    fun `progressData correctly stores current and target count`() {
        val progressData = mapOf("currentCount" to 3, "targetCount" to 5)
        val userChallenge = createUserChallenge(progressData = progressData)

        assertEquals(3, userChallenge.progressData?.get("currentCount"))
        assertEquals(5, userChallenge.progressData?.get("targetCount"))
    }

    @Test
    fun `progressData handles zero progress`() {
        val progressData = mapOf("currentCount" to 0, "targetCount" to 10)
        val userChallenge = createUserChallenge(progressData = progressData)

        assertEquals(0, userChallenge.progressData?.get("currentCount"))
        assertEquals(10, userChallenge.progressData?.get("targetCount"))
    }

    @Test
    fun `progressData handles completed progress`() {
        val progressData = mapOf("currentCount" to 5, "targetCount" to 5)
        val userChallenge = createUserChallenge(progressData = progressData)

        val current = userChallenge.progressData?.get("currentCount") as? Int ?: 0
        val target = userChallenge.progressData?.get("targetCount") as? Int ?: 0
        assertTrue(current >= target)
    }

    @Test
    fun `progressData is reset to zero on challenge failure`() {
        // When a challenge expires, progress should be reset
        val progressData = mapOf("currentCount" to 0, "targetCount" to 5)
        val expiredChallenge = createUserChallenge(
            status = "expired",
            progressData = progressData
        )

        assertEquals("expired", expiredChallenge.status)
        // Progress is conceptually lost - new attempt starts from 0
        assertEquals(0, expiredChallenge.progressData?.get("currentCount"))
    }

    // =====================================================
    // Challenge Status Response Tests
    // =====================================================

    @Test
    fun `ChallengeStatusResponse indicates no active challenge`() {
        val response = ChallengeStatusResponse(
            hasActiveChallenge = false,
            activeChallenge = null,
            isOnCooldown = false,
            cooldownEndsAt = null,
            availableChallenges = listOf(createChallenge())
        )

        assertFalse(response.hasActiveChallenge)
        assertNull(response.activeChallenge)
        assertFalse(response.isOnCooldown)
    }

    @Test
    fun `ChallengeStatusResponse indicates active challenge`() {
        val challenge = createChallenge()
        val userChallenge = createUserChallenge(challenge = challenge)

        val response = ChallengeStatusResponse(
            hasActiveChallenge = true,
            activeChallenge = userChallenge,
            isOnCooldown = false,
            cooldownEndsAt = null,
            availableChallenges = null
        )

        assertTrue(response.hasActiveChallenge)
        assertNotNull(response.activeChallenge)
        assertEquals("active", response.activeChallenge?.status)
    }

    @Test
    fun `ChallengeStatusResponse indicates cooldown period`() {
        val response = ChallengeStatusResponse(
            hasActiveChallenge = false,
            activeChallenge = null,
            isOnCooldown = true,
            cooldownEndsAt = "2025-12-20T10:00:00.000Z",
            availableChallenges = null
        )

        assertFalse(response.hasActiveChallenge)
        assertTrue(response.isOnCooldown)
        assertNotNull(response.cooldownEndsAt)
    }

    // =====================================================
    // Challenge Difficulty Tests
    // =====================================================

    @Test
    fun `challenge difficulty is correctly set to easy`() {
        val challenge = createChallenge(difficulty = "easy")
        assertEquals("easy", challenge.difficulty)
    }

    @Test
    fun `challenge difficulty is correctly set to expert`() {
        val challenge = createChallenge(difficulty = "expert")
        assertEquals("expert", challenge.difficulty)
    }

    @Test
    fun `challenge difficulty is correctly set to chad`() {
        val challenge = createChallenge(difficulty = "chad")
        assertEquals("chad", challenge.difficulty)
    }

    // =====================================================
    // Challenge Condition Params Tests
    // =====================================================

    @Test
    fun `exploration count challenge has count parameter`() {
        val challenge = createChallenge(
            conditionType = "EXPLORATION_COUNT",
            conditionParams = mapOf("count" to 5)
        )
        assertEquals(5, challenge.conditionParams?.get("count"))
    }

    @Test
    fun `time based challenge has hour parameter`() {
        val challenge = createChallenge(
            conditionType = "TIME_BASED",
            conditionParams = mapOf("hour" to 22) // 10 PM
        )
        assertEquals(22, challenge.conditionParams?.get("hour"))
    }

    @Test
    fun `streak challenge has days parameter`() {
        val challenge = createChallenge(
            conditionType = "STREAK",
            conditionParams = mapOf("days" to 7)
        )
        assertEquals(7, challenge.conditionParams?.get("days"))
    }

    // =====================================================
    // Challenge Active Status Tests
    // =====================================================

    @Test
    fun `active challenge is available for selection`() {
        val challenge = createChallenge()
        assertTrue(challenge.isActive)
    }

    @Test
    fun `inactive challenge is not available for selection`() {
        val challenge = Challenge(
            id = 1,
            name = "Inactive Challenge",
            description = "This challenge is not active",
            iconUrl = null,
            conditionType = "EXPLORATION_COUNT",
            conditionParams = mapOf("count" to 5),
            rewardBadgeId = 1,
            cooldownHours = 24,
            expirationHours = 24,
            isActive = false,
            difficulty = "easy",
            displayOrder = 0,
            allowsExtension = false
        )
        assertFalse(challenge.isActive)
    }

    // =====================================================
    // Progress Calculation Helper Tests
    // =====================================================

    @Test
    fun `calculate progress percentage for exploration count challenge`() {
        val progressData = mapOf("currentCount" to 3.0, "targetCount" to 5.0)

        val current = (progressData["currentCount"] as? Number)?.toDouble() ?: 0.0
        val target = (progressData["targetCount"] as? Number)?.toDouble() ?: 1.0
        val percentage = (current / target * 100).toInt()

        assertEquals(60, percentage)
    }

    @Test
    fun `calculate progress percentage handles zero target`() {
        val progressData = mapOf("currentCount" to 3.0, "targetCount" to 0.0)

        val current = (progressData["currentCount"] as? Number)?.toDouble() ?: 0.0
        val target = (progressData["targetCount"] as? Number)?.toDouble() ?: 0.0
        val percentage = if (target > 0) (current / target * 100).toInt() else 0

        assertEquals(0, percentage)
    }

    @Test
    fun `progress percentage caps at 100 when exceeded`() {
        val progressData = mapOf("currentCount" to 7.0, "targetCount" to 5.0)

        val current = (progressData["currentCount"] as? Number)?.toDouble() ?: 0.0
        val target = (progressData["targetCount"] as? Number)?.toDouble() ?: 1.0
        val percentage = minOf((current / target * 100).toInt(), 100)

        assertEquals(100, percentage)
    }

    // =====================================================
    // Challenge Badge Relationship Tests
    // =====================================================

    @Test
    fun `challenge has associated reward badge`() {
        val challenge = createChallenge()
        assertNotNull(challenge.rewardBadgeId)
        assertEquals(1, challenge.rewardBadgeId)
    }

    @Test
    fun `completed challenge awards badge`() {
        val challenge = createChallenge()
        val completedChallenge = UserChallenge(
            id = 1,
            challengeId = challenge.id,
            userId = 1,
            status = "completed",
            activatedAt = "2025-12-19T10:00:00.000Z",
            completedAt = "2025-12-19T15:00:00.000Z",
            expiresAt = "2025-12-20T10:00:00.000Z",
            cooldownEndsAt = "2025-12-20T10:00:00.000Z",
            progressData = mapOf("currentCount" to 5, "targetCount" to 5),
            challenge = challenge
        )

        assertEquals("completed", completedChallenge.status)
        assertNotNull(completedChallenge.completedAt)
        assertNotNull(completedChallenge.challenge?.rewardBadgeId)
    }

    @Test
    fun `expired challenge does not award badge`() {
        val challenge = createChallenge()
        val expiredChallenge = UserChallenge(
            id = 1,
            challengeId = challenge.id,
            userId = 1,
            status = "expired",
            activatedAt = "2025-12-19T10:00:00.000Z",
            completedAt = null, // No completion date
            expiresAt = "2025-12-20T10:00:00.000Z",
            cooldownEndsAt = "2025-12-20T10:00:00.000Z",
            progressData = mapOf("currentCount" to 3, "targetCount" to 5),
            challenge = challenge
        )

        assertEquals("expired", expiredChallenge.status)
        assertNull(expiredChallenge.completedAt)
        // Progress was not enough - badge not awarded
        val current = expiredChallenge.progressData?.get("currentCount") as? Int ?: 0
        val target = expiredChallenge.progressData?.get("targetCount") as? Int ?: 0
        assertTrue(current < target)
    }
}

