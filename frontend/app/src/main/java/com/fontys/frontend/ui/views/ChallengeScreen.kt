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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Challenge
import com.fontys.frontend.data.models.UserChallenge
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.ChallengeViewModel
import com.fontys.frontend.ui.viewmodels.ChallengeUiState

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
    if (challenges.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No challenges available at the moment",
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
                    onStartChallenge = {
                        viewModel.startChallenge(userId, challenge.id)
                    }
                )
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

@Composable
fun UserChallengeCard(
    userChallenge: UserChallenge,
    onProgressUpdate: (Int) -> Unit
) {
    val progress = if (userChallenge.challenge.targetValue > 0) {
        (userChallenge.currentProgress.toFloat() / userChallenge.challenge.targetValue).coerceIn(0f, 1f)
    } else {
        0f
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
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Reward",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${userChallenge.currentProgress} / ${userChallenge.challenge.targetValue}",
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
                Text(
                    text = "🏆 ${userChallenge.challenge.rewardPoints} points",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AvailableChallengeCard(
    challenge: Challenge,
    onStartChallenge: () -> Unit
) {
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
                        text = challenge.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = challenge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                    Text(
                        text = "Target: ${challenge.targetValue}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "🏆 ${challenge.rewardPoints} points",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onStartChallenge,
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start")
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
                Text(
                    text = "🏆 ${userChallenge.challenge.rewardPoints} points earned",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
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

