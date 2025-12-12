package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Badge
import com.fontys.frontend.ui.components.BadgeIcons
import com.fontys.frontend.ui.components.shimmerEffect
import com.fontys.frontend.ui.viewmodels.BadgeViewModel
import com.fontys.frontend.ui.viewmodels.BadgeUiState
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                val badgesByCategory = remember(state.badges) {
                    state.badges.groupBy { it.category }
                }
                
                // Order of categories
                val categoryOrder = listOf("Explorer", "Social", "Streak", "Special")
                val sortedCategories = badgesByCategory.keys.sortedBy { category ->
                    val index = categoryOrder.indexOfFirst { it.equals(category, ignoreCase = true) }
                    if (index != -1) index else Int.MAX_VALUE
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(top = 32.dp, bottom = 100.dp)
                ) {
                    // Profile & Circular Progress Section
                    item(span = { GridItemSpan(3) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            BadgeProfileSection(
                                earnedBadges = state.earnedBadges,
                                totalBadges = state.totalBadges
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            CircularBadgeProgress(
                                earnedBadges = state.earnedBadges,
                                totalBadges = state.totalBadges
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Categories
                    sortedCategories.forEach { category ->
                        val categoryBadges = badgesByCategory[category] ?: emptyList()
                        
                        if (categoryBadges.isNotEmpty()) {
                            item(span = { GridItemSpan(3) }) {
                                BadgeCategoryHeader(category = category)
                            }
                            
                            items(categoryBadges) { badge ->
                                BadgeItem(
                                    badge = badge,
                                    onClick = { selectedBadge = badge }
                                )
                            }
                            
                            item(span = { GridItemSpan(3) }) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
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
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refreshBadges(userId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeCategoryHeader(category: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

@Composable
fun BadgeProfileSection(earnedBadges: Int, totalBadges: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Badge Collection",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun CircularBadgeProgress(earnedBadges: Int, totalBadges: Int) {
    val progress = if (totalBadges > 0) earnedBadges.toFloat() / totalBadges else 0f
    val percentage = (progress * 100).toInt()
    
    Box(contentAlignment = Alignment.Center) {
        // Background Circle (Track)
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(120.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round,
        )
        
        // Progress Circle
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(120.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round,
        )
        
        // Text inside
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$earnedBadges / $totalBadges",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BadgeItem(badge: Badge, onClick: () -> Unit) {
    val modifier = if (!badge.isUnlocked) {
        Modifier.shimmerEffect(shimmerColor = Color.White.copy(alpha = 0.3f))
    } else {
        Modifier
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = if (badge.isUnlocked) 0.dp else 1.dp,
                color = if (badge.isUnlocked) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (badge.isUnlocked) 8.dp else 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier), // Apply shimmer if locked
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = BadgeIcons.getIcon(badge.name, badge.id),
                        contentDescription = badge.name,
                        tint = if (badge.isUnlocked)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                // Name
                Text(
                    text = badge.name,
                    fontSize = 10.sp,
                    fontWeight = if (badge.isUnlocked) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                    color = if (badge.isUnlocked)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lock Overlay
            if (!badge.isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.05f)) // Subtle darkening
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
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
            val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
            val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            "${localDateTime.format(dateFormatter)} at ${localDateTime.format(timeFormatter)}"
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
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = BadgeIcons.getIcon(badge.name, badge.id),
                            contentDescription = null,
                            tint = if (badge.isUnlocked)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )

                        if (!badge.isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
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

                // Progress section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROGRESS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${badge.currentProgress} / ${badge.maxProgress}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress bar
                        val badgeProgress = if (badge.maxProgress > 0)
                            badge.currentProgress.toFloat() / badge.maxProgress
                        else 0f
                        val badgePercentage = (badgeProgress * 100).toInt()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(badgeProgress.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )

                            Text(
                                text = "$badgePercentage%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badgeProgress > 0.4f)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
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
                    "CLOSE",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    )
}
