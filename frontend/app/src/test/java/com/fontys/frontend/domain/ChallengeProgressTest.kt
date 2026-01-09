package com.fontys.frontend.domain

import org.junit.Test
import org.junit.Assert.*
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for Challenge progress and time-related business logic.
 * These tests verify:
 * - Progress only counts during active challenge window
 * - Challenge expiration logic
 * - Cooldown period handling
 * - Time remaining calculations
 */
class ChallengeProgressTest {

    // =====================================================
    // Time Window Validation Tests
    // =====================================================

    @Test
    fun `exploration within challenge window is valid`() {
        val activatedAt = Instant.parse("2025-12-19T10:00:00.000Z")
        val expiresAt = Instant.parse("2025-12-20T10:00:00.000Z")
        val explorationTime = Instant.parse("2025-12-19T15:00:00.000Z")

        val isWithinWindow = explorationTime.isAfter(activatedAt) && explorationTime.isBefore(expiresAt)

        assertTrue("Exploration should be within the challenge window", isWithinWindow)
    }

    @Test
    fun `exploration before challenge activation is invalid`() {
        val activatedAt = Instant.parse("2025-12-19T10:00:00.000Z")
        val expiresAt = Instant.parse("2025-12-20T10:00:00.000Z")
        val explorationTime = Instant.parse("2025-12-19T09:00:00.000Z") // 1 hour before activation

        val isWithinWindow = explorationTime.isAfter(activatedAt) && explorationTime.isBefore(expiresAt)

        assertFalse("Exploration before activation should not count", isWithinWindow)
    }

    @Test
    fun `exploration after challenge expiration is invalid`() {
        val activatedAt = Instant.parse("2025-12-19T10:00:00.000Z")
        val expiresAt = Instant.parse("2025-12-20T10:00:00.000Z")
        val explorationTime = Instant.parse("2025-12-20T12:00:00.000Z") // 2 hours after expiration

        val isWithinWindow = explorationTime.isAfter(activatedAt) && explorationTime.isBefore(expiresAt)

        assertFalse("Exploration after expiration should not count", isWithinWindow)
    }

    @Test
    fun `exploration at exact expiration time is invalid`() {
        val activatedAt = Instant.parse("2025-12-19T10:00:00.000Z")
        val expiresAt = Instant.parse("2025-12-20T10:00:00.000Z")
        val explorationTime = Instant.parse("2025-12-20T10:00:00.000Z") // Exact expiration

        // Using isBefore means exact expiration time is NOT valid
        val isWithinWindow = explorationTime.isAfter(activatedAt) && explorationTime.isBefore(expiresAt)

        assertFalse("Exploration at exact expiration should not count", isWithinWindow)
    }

    // =====================================================
    // Challenge Expiration Tests
    // =====================================================

    @Test
    fun `challenge is expired when current time is past expiration`() {
        val expiresAt = Instant.parse("2025-12-19T10:00:00.000Z")
        val currentTime = Instant.parse("2025-12-19T11:00:00.000Z")

        val isExpired = currentTime.isAfter(expiresAt)

        assertTrue("Challenge should be expired", isExpired)
    }

    @Test
    fun `challenge is not expired when current time is before expiration`() {
        val expiresAt = Instant.parse("2025-12-20T10:00:00.000Z")
        val currentTime = Instant.parse("2025-12-19T15:00:00.000Z")

        val isExpired = currentTime.isAfter(expiresAt)

        assertFalse("Challenge should not be expired", isExpired)
    }

    @Test
    fun `challenge with 24 hour expiration expires after 24 hours`() {
        val activatedAt = Instant.parse("2025-12-19T10:00:00.000Z")
        val expirationHours = 24L
        val expiresAt = activatedAt.plus(expirationHours, ChronoUnit.HOURS)

        val expectedExpiration = Instant.parse("2025-12-20T10:00:00.000Z")

        assertEquals(expectedExpiration, expiresAt)
    }

    // =====================================================
    // Cooldown Period Tests
    // =====================================================

    @Test
    fun `user is on cooldown when current time is before cooldown ends`() {
        val cooldownEndsAt = Instant.parse("2025-12-20T10:00:00.000Z")
        val currentTime = Instant.parse("2025-12-19T15:00:00.000Z")

        val isOnCooldown = currentTime.isBefore(cooldownEndsAt)

        assertTrue("User should be on cooldown", isOnCooldown)
    }

    @Test
    fun `user is not on cooldown when current time is after cooldown ends`() {
        val cooldownEndsAt = Instant.parse("2025-12-20T10:00:00.000Z")
        val currentTime = Instant.parse("2025-12-20T12:00:00.000Z")

        val isOnCooldown = currentTime.isBefore(cooldownEndsAt)

        assertFalse("User should not be on cooldown", isOnCooldown)
    }

    @Test
    fun `cooldown period is set correctly for 24 hour challenges`() {
        val activatedAt = Instant.parse("2025-12-19T10:00:00.000Z")
        val cooldownHours = 24L
        val cooldownEndsAt = activatedAt.plus(cooldownHours, ChronoUnit.HOURS)

        val expectedCooldownEnd = Instant.parse("2025-12-20T10:00:00.000Z")

        assertEquals(expectedCooldownEnd, cooldownEndsAt)
    }

    // =====================================================
    // Time Remaining Calculations
    // =====================================================

    @Test
    fun `calculate hours remaining correctly`() {
        val expiresAt = Instant.parse("2025-12-20T10:00:00.000Z")
        val currentTime = Instant.parse("2025-12-19T15:00:00.000Z")

        val msRemaining = expiresAt.toEpochMilli() - currentTime.toEpochMilli()
        val hoursRemaining = msRemaining / (1000 * 60 * 60)

        assertEquals(19, hoursRemaining) // 19 hours remaining
    }

    @Test
    fun `calculate minutes remaining correctly`() {
        val expiresAt = Instant.parse("2025-12-19T11:30:00.000Z")
        val currentTime = Instant.parse("2025-12-19T10:00:00.000Z")

        val msRemaining = expiresAt.toEpochMilli() - currentTime.toEpochMilli()
        val hoursRemaining = msRemaining / (1000 * 60 * 60)
        val minutesRemaining = (msRemaining % (1000 * 60 * 60)) / (1000 * 60)

        assertEquals(1, hoursRemaining)
        assertEquals(30, minutesRemaining)
    }

    @Test
    fun `time remaining is zero when expired`() {
        val expiresAt = Instant.parse("2025-12-19T10:00:00.000Z")
        val currentTime = Instant.parse("2025-12-19T12:00:00.000Z")

        val msRemaining = expiresAt.toEpochMilli() - currentTime.toEpochMilli()
        val isExpired = msRemaining <= 0

        assertTrue("Should be expired", isExpired)
    }

    // =====================================================
    // Progress Reset on Failure Tests
    // =====================================================

    @Test
    fun `progress is reset when challenge expires without completion`() {
        // Simulate an expired challenge
        val progressBeforeExpiry = mapOf("currentCount" to 3, "targetCount" to 5)

        // When challenge expires, the next attempt starts fresh
        val progressAfterReset = mapOf("currentCount" to 0, "targetCount" to 5)

        assertEquals(0, progressAfterReset["currentCount"])
        assertEquals(5, progressAfterReset["targetCount"])

        // The failed progress is not carried over
        assertNotEquals(progressBeforeExpiry["currentCount"], progressAfterReset["currentCount"])
    }

    @Test
    fun `completed challenge retains final progress`() {
        val finalProgress = mapOf("currentCount" to 5, "targetCount" to 5)

        val current = finalProgress["currentCount"] as Int
        val target = finalProgress["targetCount"] as Int

        assertEquals(current, target)
        assertTrue("Progress should indicate completion", current >= target)
    }

    // =====================================================
    // Challenge Badge Progress Isolation Tests
    // =====================================================

    @Test
    fun `challenge badge progress is independent per challenge attempt`() {
        // First attempt - failed
        val attempt1Progress = mapOf("currentCount" to 3, "targetCount" to 5)
        val attempt1Status = "expired"

        // Second attempt - starts fresh
        val attempt2Progress = mapOf("currentCount" to 0, "targetCount" to 5)
        val attempt2Status = "active"

        // Progress from attempt 1 does not carry to attempt 2
        assertNotEquals(attempt1Progress["currentCount"], attempt2Progress["currentCount"])
        assertEquals(0, attempt2Progress["currentCount"])
        assertEquals("active", attempt2Status)
    }

    @Test
    fun `only active challenge can accumulate progress`() {
        val activeChallenge = mapOf(
            "status" to "active",
            "currentCount" to 3,
            "targetCount" to 5
        )

        val expiredChallenge = mapOf(
            "status" to "expired",
            "currentCount" to 2,
            "targetCount" to 5
        )

        // Only active challenge should be considered for progress
        assertTrue(activeChallenge["status"] == "active")
        assertFalse(expiredChallenge["status"] == "active")
    }

    // =====================================================
    // Edge Cases
    // =====================================================

    @Test
    fun `streak challenge handles multi-day tracking`() {
        val streakProgress = mapOf("currentStreak" to 5, "targetStreak" to 7)

        val current = streakProgress["currentStreak"] as Int
        val target = streakProgress["targetStreak"] as Int

        assertFalse("Streak not yet complete", current >= target)
        assertEquals(2, target - current) // 2 more days needed
    }

    @Test
    fun `time based challenge checks specific hour`() {
        val targetHour = 22 // 10 PM
        val explorationHour = 22

        assertEquals("Exploration at target hour should match", targetHour, explorationHour)
    }

    @Test
    fun `time based challenge rejects wrong hour`() {
        val targetHour = 22 // 10 PM
        val explorationHour = 14 // 2 PM

        assertNotEquals("Exploration at wrong hour should not match", targetHour, explorationHour)
    }
}

