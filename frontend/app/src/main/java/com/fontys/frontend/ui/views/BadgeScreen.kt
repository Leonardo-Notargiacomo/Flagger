package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Badge
import com.fontys.frontend.ui.viewmodels.BadgeViewModel
import com.fontys.frontend.ui.viewmodels.BadgeUiState
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Header
        BadgeHeader()

        when (val state = uiState) {
            is BadgeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            is BadgeUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile/Badge display section
                    BadgeProfileSection(
                        earnedBadges = state.earnedBadges,
                        totalBadges = state.totalBadges
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Progress indicator with decorative line
                    BadgeProgressLine(
                        earnedBadges = state.earnedBadges,
                        totalBadges = state.totalBadges
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Badge grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
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
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = Color(0xFF8B4513),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refreshBadges(userId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Decorative dots on the left
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }

        // Title - centered
        Text(
            text = "BADGES",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        // Decorative dots on the right
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun BadgeProfileSection(earnedBadges: Int, totalBadges: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge/Profile image container
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏆",
                fontSize = 64.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Badge count display
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "BADGE COLLECTION",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun BadgeProgressLine(earnedBadges: Int, totalBadges: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Left line
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Center circle indicator
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )

            // Right line
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$earnedBadges / $totalBadges Unlocked",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
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
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = if (badge.isUnlocked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (badge.isUnlocked) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                Color(0xFFD4C5B0) // Slightly darker cream for locked badges
        ),
        shape = RoundedCornerShape(16.dp)
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
                    fontSize = 10.sp,
                    fontWeight = if (badge.isUnlocked) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    color = if (badge.isUnlocked)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.5f)
                )
            }

            // Lock icon for locked badges in top-right corner
            if (!badge.isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 16.sp
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
    // Format the date if available
    val formattedDate = badge.unlockedAt?.let { dateString ->
        try {
            val zonedDateTime = ZonedDateTime.parse(dateString)
            val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            "${zonedDateTime.format(dateFormatter)} at ${zonedDateTime.format(timeFormatter)}"
        } catch (_: Exception) {
            dateString // Fallback to original string if parsing fails
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(24.dp),
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
                        .size(120.dp)
                        .border(
                            width = 3.dp,
                            color = if (badge.isUnlocked)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(20.dp)
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
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badge category
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = badge.category.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = if (badge.isUnlocked)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
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
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (badge.isUnlocked) "UNLOCKED!" else "LOCKED",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = if (badge.isUnlocked)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (badge.isUnlocked && formattedDate != null) {
                                Text(
                                    text = "Earned: $formattedDate",
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
                    text = "UNLOCK CRITERIA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = badge.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "CLOSE",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    )
}

