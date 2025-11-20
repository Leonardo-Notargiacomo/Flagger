package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Challenge
import com.fontys.frontend.data.models.ChallengeType
import com.fontys.frontend.data.models.UserChallenge
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.ChallengeViewModel
import com.fontys.frontend.ui.viewmodels.ChallengeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Composable
fun ChallengeScreen(
    userId: Int = UserRepository.userId,
    viewModel: ChallengeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeChallenges by viewModel.activeChallenges.collectAsState()
    val completedChallenges by viewModel.completedChallenges.collectAsState()
    val availableChallenges by viewModel.availableChallenges.collectAsState()
    val showCompletionDialog by viewModel.showCompletionDialog.collectAsState()
    val completionData by viewModel.completionData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(userId) {
        viewModel.loadUserChallenges(userId)
        viewModel.loadAvailableChallenges()
    }

    // Show completion dialog when challenge is completed
    if (showCompletionDialog && completionData != null) {
        ChallengeCompletionDialog(
            completionData = completionData!!,
            onDismiss = { viewModel.dismissCompletionDialog() }
        )
    }

    // Show error message if any
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(error)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Challenges",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Complete challenges to earn rewards",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active (${activeChallenges.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Available (${availableChallenges.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Completed (${completedChallenges.size})") }
            )
        }

        // Content
        when (uiState) {
            is ChallengeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ChallengeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as ChallengeUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.loadUserChallenges(userId)
                            viewModel.loadAvailableChallenges()
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is ChallengeUiState.Success -> {
                when (selectedTab) {
                    0 -> ActiveChallengesTab(activeChallenges, viewModel, userId)
                    1 -> AvailableChallengesTab(availableChallenges, viewModel, userId)
                    2 -> CompletedChallengesTab(completedChallenges)
                }
            }
        }
    }
}

@Composable
fun ActiveChallengesTab(
    challenges: List<UserChallenge>,
    viewModel: ChallengeViewModel,
    userId: Int
) {
    if (challenges.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No active challenges. Start one from the Available tab!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges) { userChallenge ->
                UserChallengeCard(
                    userChallenge = userChallenge,
                    onProgressUpdate = { progress ->
                        viewModel.updateChallengeProgress(userId, userChallenge.challenge.id, progress)
                    }
                )
            }
        }
    }
}

@Composable
fun AvailableChallengesTab(
    challenges: List<Challenge>,
    viewModel: ChallengeViewModel,
    userId: Int
) {
    val canSelectChallenge by viewModel.canSelectChallenge.collectAsState()
    val timeUntilNextSelection by viewModel.timeUntilNextSelection.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Show cooldown message if user can't select a challenge
        if (!canSelectChallenge) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You can choose a new challenge in ${formatTimeRemaining(timeUntilNextSelection)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (challenges.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No available challenges right now.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(challenges) { challenge ->
                    AvailableChallengeCard(
                        challenge = challenge,
                        canSelect = canSelectChallenge,
                        onStartChallenge = {
                            if (canSelectChallenge) {
                                viewModel.startChallenge(userId, challenge.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedChallengesTab(challenges: List<UserChallenge>) {
    if (challenges.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No completed challenges yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges) { userChallenge ->
                CompletedChallengeCard(userChallenge = userChallenge)
            }
        }
    }
}

// Companion object to store challenge timers globally
private object ChallengeTimerCache {
    private val timers = mutableMapOf<Int, MutableStateFlow<Long>>()
    private val startTimes = mutableMapOf<Int, Long>()
    private val timerJobs = mutableMapOf<Int, kotlinx.coroutines.Job>()

    @Synchronized
    fun getOrCreateTimer(challengeId: Int, startedAt: String?): StateFlow<Long> {
        // Check if timer already exists - if so, return existing flow
        if (timers.containsKey(challengeId)) {
            // Timer already exists, just return the existing flow
            return timers[challengeId]!!.asStateFlow()
        }

        // Timer doesn't exist, create new one
        // If startedAt is null, use current time as fallback
        val startTime = if (startedAt != null) {
            parseDateTime(startedAt)
        } else {
            System.currentTimeMillis()
        }

        startTimes[challengeId] = startTime
        timers[challengeId] = MutableStateFlow(System.currentTimeMillis())

        // Start a global timer for this challenge
        timerJobs[challengeId] = GlobalScope.launch {
            while (true) {
                timers[challengeId]?.value = System.currentTimeMillis()
                delay(1000)

                // Check if expired
                val cachedStartTime = startTimes[challengeId]
                if (cachedStartTime != null) {
                    val expiresAt = cachedStartTime + (24 * 60 * 60 * 1000L)
                    if (System.currentTimeMillis() >= expiresAt) {
                        break
                    }
                } else {
                    break
                }
            }
        }

        return timers[challengeId]!!.asStateFlow()
    }

    fun removeTimer(challengeId: Int) {
        timerJobs[challengeId]?.cancel()
        timerJobs.remove(challengeId)
        timers.remove(challengeId)
        startTimes.remove(challengeId)
    }

    fun getStartTime(challengeId: Int): Long {
        return startTimes[challengeId] ?: System.currentTimeMillis()
    }
}

@Composable
fun UserChallengeCard(
    userChallenge: UserChallenge,
    onProgressUpdate: (Int) -> Unit
) {
    val challengeType = userChallenge.challenge.getType()

    // Total duration used for time-based challenges (24 hours)
    val totalMillis = 24 * 60 * 60 * 1000L

    // Get the global timer for this challenge - NO REMEMBER, cache handles everything
    val currentTimeFlow = ChallengeTimerCache.getOrCreateTimer(userChallenge.id, userChallenge.startedAt)
    val currentTime by currentTimeFlow.collectAsState()

    // Calculate start time directly from cache
    val startedAtMillis = ChallengeTimerCache.getStartTime(userChallenge.id)

    val timeRemaining = remember(currentTime, startedAtMillis) {
        val expiresAtMillis = startedAtMillis + totalMillis
        (expiresAtMillis - currentTime).coerceAtLeast(0)
    }

    // Clean up timer when challenge is completed
    DisposableEffect(userChallenge.isCompleted) {
        onDispose {
            if (userChallenge.isCompleted) {
                ChallengeTimerCache.removeTimer(userChallenge.id)
            }
        }
    }

    // Compute progress fraction correctly
    val progressFraction = when (challengeType) {
        ChallengeType.TIME_BASED -> {
            val elapsed = (totalMillis - timeRemaining).coerceAtLeast(0L)
            elapsed.toFloat() / totalMillis
        }
        ChallengeType.COUNT -> {
            val target = userChallenge.challenge.targetValue
            if (target > 0) userChallenge.currentProgress.toFloat() / target else 0f
        }
        ChallengeType.STREAK -> {
            val target = userChallenge.challenge.targetValue
            if (target > 0) userChallenge.currentProgress.toFloat() / target else 0f
        }
    }

    // Display-friendly progress text
    val progressText = when (challengeType) {
        ChallengeType.TIME_BASED -> "${userChallenge.currentProgress} / 1"
        ChallengeType.COUNT -> "${userChallenge.currentProgress} / ${userChallenge.challenge.targetValue}"
        ChallengeType.STREAK -> "${userChallenge.currentProgress} / ${userChallenge.challenge.targetValue} days"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userChallenge.challenge.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userChallenge.challenge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (userChallenge.challenge.badgeId != null) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Badge Reward",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar and details
            Column {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Show progress text for all challenge types
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show timer icon and remaining time for time-based challenges
                if (challengeType == ChallengeType.TIME_BASED && timeRemaining > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Time remaining",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTimeRemaining(timeRemaining),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (challengeType == ChallengeType.STREAK) {
                    Text(
                        text = "🔥 Daily streak challenge",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (challengeType == ChallengeType.COUNT) {
                    Text(
                        text = "📊 Count challenge",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userChallenge.challenge.badgeId != null) {
                        Text(
                            text = "🏅 Badge reward",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFD700)
                        )
                    }

                    // Provide a small action to update progress for COUNT and STREAK challenges
                    if (challengeType == ChallengeType.COUNT || challengeType == ChallengeType.STREAK) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { onProgressUpdate(userChallenge.currentProgress + 1) },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Add progress")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableChallengeCard(
    challenge: Challenge,
    canSelect: Boolean,
    onStartChallenge: () -> Unit
) {
    val challengeType = challenge.getType()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canSelect) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .then(if (!canSelect) Modifier else Modifier)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (canSelect) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = challenge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (canSelect) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                if (challenge.badgeId != null) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Badge Reward",
                        tint = if (canSelect) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val targetText = when (challengeType) {
                        ChallengeType.TIME_BASED -> "Complete within 24 hours"
                        ChallengeType.COUNT -> "Target: ${challenge.targetValue}"
                        ChallengeType.STREAK -> "Streak: ${challenge.targetValue} days"
                    }

                    Text(
                        text = targetText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (canSelect) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )

                    if (challenge.badgeId != null) {
                        Text(
                            text = "🏅 Badge reward",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (canSelect) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.4f)
                        )
                    }
                }

                Button(
                    onClick = onStartChallenge,
                    modifier = Modifier.height(40.dp),
                    enabled = canSelect
                ) {
                    if (!canSelect) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (canSelect) "Start" else "Locked")
                }
            }
        }
    }
}

@Composable
fun CompletedChallengeCard(userChallenge: UserChallenge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userChallenge.challenge.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userChallenge.challenge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Completed: ${userChallenge.completedAt ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
                if (userChallenge.challenge.badgeId != null) {
                    Text(
                        text = "🏅 Badge earned",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFD700)
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeCompletionDialog(
    completionData: com.fontys.frontend.data.models.ChallengeCompletionResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Achievement",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Challenge Completed!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Congratulations! You've completed the challenge:")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = completionData.challenge.challenge.name,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("You earned ${completionData.pointsAwarded} points!")

                completionData.newBadge?.let { badge ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🏅 New Badge Unlocked: ${badge.name}",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Awesome!")
            }
        }
    )
}

// Helper function to parse date/time strings
private fun parseDateTime(dateString: String): Long {
    return try {
        // Try to parse ISO 8601 format (e.g., "2025-11-20T10:30:00Z")
        Instant.parse(dateString).toEpochMilli()
    } catch (_: Exception) {
        try {
            // Fallback to another format if needed
            LocalDateTime.parse(dateString).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}

// Helper function to format time remaining
private fun formatTimeRemaining(milliseconds: Long): String {
    if (milliseconds <= 0) return "Expired"

    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

    // Format as HH:MM:SS or HH:MM for cleaner display
    return when {
        hours > 0 -> String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        minutes > 0 -> String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
        else -> "${seconds}s"
    }
}
