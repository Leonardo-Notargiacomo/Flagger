package com.fontys.frontend.ui.views

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Badge
import com.fontys.frontend.data.models.Challenge
import com.fontys.frontend.data.models.ChallengeCompletionResult
import com.fontys.frontend.data.models.ChallengeType
import com.fontys.frontend.data.models.UserChallenge
import com.fontys.frontend.ui.viewmodels.ChallengeViewModel
import com.fontys.frontend.ui.viewmodels.ChallengeUiState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeChallenge by viewModel.activeChallenge.collectAsState()
    val history by viewModel.history.collectAsState()
    val availableChallenges by viewModel.availableChallenges.collectAsState()
    val isOnCooldown by viewModel.isOnCooldown.collectAsState()
    val cooldownEndsAt by viewModel.cooldownEndsAt.collectAsState()
    val showCompletionDialog by viewModel.showCompletionDialog.collectAsState()
    val completionData by viewModel.completionData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // Refresh when active challenge changes (e.g., when user accepts a challenge)
    LaunchedEffect(activeChallenge?.id) {
        if (activeChallenge != null) {
            viewModel.refresh()
        }
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
        // Header section matching BadgeScreen style
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Challenge Collection",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tabs (matching FriendsScreen style)
        var selectedTab by remember { mutableStateOf(0) }
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "Active",
                        fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Available",
                        fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        "Completed",
                        fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
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
                        Button(
                            onClick = viewModel::refresh,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Text(
                                "Retry",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
            is ChallengeUiState.Success -> {
                when (selectedTab) {
                    0 -> ActiveChallengesTab(activeChallenge, onCompleteCheck = viewModel::checkCompletion)
                    1 -> AvailableChallengesTab(
                        challenges = availableChallenges,
                        isOnCooldown = isOnCooldown,
                        hasActiveChallenge = activeChallenge != null,
                        cooldownEndsAt = cooldownEndsAt,
                        onSelect = viewModel::selectChallenge
                    )
                    2 -> CompletedChallengesTab(history.filter { it.status == "completed" })
                }
            }
        }
    }
}

@Composable
fun ActiveChallengesTab(activeChallenge: UserChallenge?, onCompleteCheck: () -> Unit) {
    // Auto-refresh progress every 30 seconds
    LaunchedEffect(activeChallenge?.id) {
        if (activeChallenge != null) {
            while (true) {
                kotlinx.coroutines.delay(30000L) // Refresh every 30 seconds
                onCompleteCheck()
            }
        }
    }

    if (activeChallenge == null) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserChallengeCard(activeChallenge)
            Button(
                onClick = onCompleteCheck,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Check Progress",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun AvailableChallengesTab(
    challenges: List<Challenge>,
    isOnCooldown: Boolean,
    hasActiveChallenge: Boolean,
    cooldownEndsAt: String?,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Show lock message if user can't select a challenge
        if (isOnCooldown || hasActiveChallenge) {
            val timeRemaining = remember(cooldownEndsAt) { cooldownEndsAt?.let { parseDateTime(it) - System.currentTimeMillis() } ?: 0L }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
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
                        text = if (isOnCooldown) {
                            "You can choose a new challenge in ${formatTimeRemaining(timeRemaining)}"
                        } else {
                            "Complete your active challenge before starting a new one"
                        },
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
                contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(challenges) { challenge ->
                    AvailableChallengeCard(
                        challenge = challenge,
                        canSelect = !isOnCooldown && !hasActiveChallenge,
                        onSelect = {
                            onSelect(challenge.id)
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
            contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(challenges) { userChallenge ->
                CompletedChallengeCard(userChallenge = userChallenge)
            }
        }
    }
}

@Composable
fun UserChallengeCard(
    userChallenge: UserChallenge
) {
    val challengeType = userChallenge.challenge?.getType() ?: ChallengeType.TIME_BASED
    val expiresAt = remember(userChallenge.expiresAt) { userChallenge.expiresAt?.let(::parseDateTime) }
    val activatedAt = remember(userChallenge.activatedAt) { userChallenge.activatedAt?.let(::parseDateTime) }

    // All challenges have a 24-hour expiration timer, update every second
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(expiresAt) {
        if (expiresAt != null) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val expiresIn = remember(activatedAt, expiresAt, currentTime) {
        val timeRemaining = if (activatedAt == null || expiresAt == null) 0L else (expiresAt - currentTime).coerceAtLeast(0L)
        Log.d("ChallengeTimer", "Challenge: ${userChallenge.challenge?.name}, expiresAt: ${userChallenge.expiresAt}, activatedAt: ${userChallenge.activatedAt}, expiresIn: $timeRemaining")
        timeRemaining
    }

    val progressFraction = when (challengeType) {
        ChallengeType.TIME_BASED -> {
            if (activatedAt != null && expiresAt != null && expiresAt > activatedAt) {
                val total = expiresAt - activatedAt
                (total - expiresIn).toFloat() / total.toFloat()
            } else 0f
        }
        ChallengeType.COUNT -> {
            Log.d("ChallengeUI", "COUNT challenge - full progressData: ${userChallenge.progressData}")
            val current = (userChallenge.progressData?.get("currentCount") as? Number)?.toFloat() ?: 0f
            val target = (userChallenge.challenge?.conditionParams?.get("count") as? Number)?.toFloat() ?: 1f
            Log.d("ChallengeUI", "COUNT challenge - current: $current, target: $target")
            (current / target).coerceIn(0f, 1f)
        }
        ChallengeType.STREAK -> {
            val current = (userChallenge.progressData?.get("currentStreak") as? Number)?.toFloat() ?: 0f
            val target = (userChallenge.challenge?.conditionParams?.get("days") as? Number)?.toFloat() ?: 1f
            (current / target).coerceIn(0f, 1f)
        }
    }

    val progressText = when (challengeType) {
        ChallengeType.TIME_BASED -> if (userChallenge.completedAt != null) "Completed" else formatTimeRemaining(expiresIn)
        ChallengeType.COUNT -> {
            val current = (userChallenge.progressData?.get("currentCount") as? Number)?.toInt() ?: 0
            val target = (userChallenge.challenge?.conditionParams?.get("count") as? Number)?.toInt() ?: 0
            "$current / $target"
        }
        ChallengeType.STREAK -> {
            val current = (userChallenge.progressData?.get("currentStreak") as? Number)?.toInt() ?: 0
            val target = (userChallenge.challenge?.conditionParams?.get("days") as? Number)?.toInt() ?: 0
            "$current / $target days"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
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
                        text = userChallenge.challenge?.name ?: "Unknown Challenge",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userChallenge.challenge?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (userChallenge.challenge?.rewardBadgeId != null) {
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
                // All challenges have a 24-hour timer
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show challenge type icon
                    when (challengeType) {
                        ChallengeType.STREAK -> Text("🔥 ", style = MaterialTheme.typography.bodyMedium)
                        ChallengeType.COUNT -> Text("📊 ", style = MaterialTheme.typography.bodyMedium)
                        ChallengeType.TIME_BASED -> {}
                    }

                    // Always show the 24-hour timer
                    if (expiresIn > 0) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Time remaining",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTimeRemaining(expiresIn),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userChallenge.challenge?.rewardBadgeId != null) {
                        Text(
                            text = "🏅 Badge reward",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFD700)
                        )
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
    onSelect: () -> Unit
) {
    val challengeType = challenge.getType()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (canSelect) 0.dp else 1.dp,
                color = if (canSelect)
                    Color.Transparent
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (canSelect) 8.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canSelect) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp)
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
                if (challenge.rewardBadgeId != null) {
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
                        ChallengeType.COUNT -> "Target: ${(challenge.conditionParams?.get("count") as? Number)?.toInt() ?: 0}"
                        ChallengeType.STREAK -> "Streak: ${(challenge.conditionParams?.get("days") as? Number)?.toInt() ?: 0} days"
                    }

                    Text(
                        text = targetText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (canSelect) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )

                    if (challenge.rewardBadgeId != null) {
                        Text(
                            text = "🏅 Badge reward",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (canSelect) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.4f)
                        )
                    }
                }

                Button(
                    onClick = onSelect,
                    modifier = Modifier.height(40.dp),
                    enabled = canSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
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
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
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
                        text = userChallenge.challenge?.name ?: "Unknown Challenge",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userChallenge.challenge?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                if (userChallenge.challenge?.rewardBadgeId != null) {
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
    completionData: ChallengeCompletionResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(24.dp),
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column {
                Text(
                    text = "Congratulations! You've completed your challenge!",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We notified you about any rewards.",
                    color = MaterialTheme.colorScheme.onBackground
                )

                completionData.badge?.let { badge ->
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
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "AWESOME!",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
