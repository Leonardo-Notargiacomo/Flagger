package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Badge
import com.fontys.frontend.ui.viewmodels.BadgeViewModel
import com.fontys.frontend.ui.viewmodels.BadgeUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeScreen(
    userId: Int,
    viewModel: BadgeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    LaunchedEffect(userId) {
        viewModel.loadUserBadges(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Badges", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is BadgeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is BadgeUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats card
                    BadgeStatsCard(
                        earnedBadges = state.earnedBadges,
                        totalBadges = state.totalBadges
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section title
                    Text(
                        text = "All Badges",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.badges) { badge ->
                            BadgeItem(
                                badge = badge,
                                onClick = { selectedBadge = badge }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Show badge detail dialog when a badge is selected
                selectedBadge?.let { badge ->
                    BadgeDetailDialog(
                        badge = badge,
                        onDismiss = { selectedBadge = null }
                    )
                }
            }

            is BadgeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshBadges(userId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeStatsCard(earnedBadges: Int, totalBadges: Int) {
    val progress = if (totalBadges > 0) earnedBadges.toFloat() / totalBadges else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Your Progress",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$earnedBadges / $totalBadges badges earned",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 6.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeItem(badge: Badge, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (badge.isUnlocked) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = badge.iconUrl ?: "🏆",
                    fontSize = 36.sp,
                    modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.3f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = badge.name,
                    fontSize = 11.sp,
                    fontWeight = if (badge.isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    color = if (badge.isUnlocked)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.5f)
                )
            }

            // Lock icon for locked badges
            if (!badge.isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 12.sp,
                        modifier = Modifier.alpha(0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeDetailDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge icon
                Card(
                    modifier = Modifier
                        .size(100.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge.iconUrl ?: "🏆",
                            fontSize = 56.sp,
                            modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.3f)
                        )

                        if (!badge.isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Text(
                                    text = "🔒",
                                    fontSize = 20.sp,
                                    modifier = Modifier.alpha(0.6f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Badge name
                Text(
                    text = badge.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badge category
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = badge.category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (badge.isUnlocked) "✓" else "✗",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.isUnlocked)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (badge.isUnlocked) "Unlocked!" else "Locked",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (badge.isUnlocked)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                            if (badge.isUnlocked && badge.unlockedAt != null) {
                                Text(
                                    text = "Earned: ${badge.unlockedAt}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Unlock Criteria",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = badge.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Close", fontWeight = FontWeight.Medium)
            }
        }
    )
}

