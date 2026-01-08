package com.fontys.frontend.ui.views

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Flag
import com.fontys.frontend.data.models.User
import com.fontys.frontend.ui.viewmodels.FriendRequestStatus
import com.fontys.frontend.ui.viewmodels.PublicProfileViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

/**
 * Opens a location in Google Maps or falls back to browser if Maps is not installed.
 *
 * @param context Android context for launching intents
 * @param latitude Location latitude
 * @param longitude Location longitude
 * @param locationName Optional name to display for the location
 */
private fun openLocationInGoogleMaps(
    context: Context,
    latitude: Double,
    longitude: Double,
    locationName: String = "Location"
) {
    try {
        // Use https URL scheme with location name for better place recognition
        // Including the name helps Google Maps find the actual place instead of just coordinates
        val encodedName = Uri.encode(locationName)
        val uri = "https://www.google.com/maps/search/?api=1&query=$encodedName&query_place_id=$latitude,$longitude".toUri()

        val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            // Let Android decide the best app (usually Google Maps)
            // Using minimal flags to prevent flickering
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }

        context.startActivity(mapsIntent)
    } catch (e: Exception) {
        android.util.Log.e("PublicProfileScreen", "Error opening Google Maps", e)
    }
}

/**
 * Public Profile Screen - Minimalistic design inspired by AllTrails
 *
 * Features conscious delight factors:
 * - Clean visual hierarchy with strategic use of space
 * - Prominent stats display for exploration count
 * - Clear call-to-action buttons with appropriate visual weight
 * - Pull-to-refresh for easy content updates
 *
 * Features subconscious comfort factors:
 * - Smooth animations with natural easing curves (300ms standard)
 * - Gestalt principles for natural grouping
 * - Consistent spacing system (8dp grid)
 * - Reduced cognitive load through progressive disclosure
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PublicProfileScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToMap: ((Double, Double) -> Unit)? = null,
    viewModel: PublicProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadUserProfile(userId)
        }
    )

    // Load profile on composition
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    // Stop refreshing when loading completes
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    // Animated content visibility
    val contentAlpha by animateFloatAsState(
        targetValue = if (uiState.isLoading && !isRefreshing) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )

    // Navigate back after successful friend request
    var previousStatus by remember { mutableStateOf<FriendRequestStatus>(FriendRequestStatus.NotSent) }
    LaunchedEffect(uiState.friendRequestStatus) {
        if (previousStatus is FriendRequestStatus.Sending &&
            uiState.friendRequestStatus is FriendRequestStatus.Pending) {
            // Wait for celebration animation to complete (2.5s) then navigate back
            kotlinx.coroutines.delay(2600)
            onNavigateBack()
        }
        previousStatus = uiState.friendRequestStatus
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with back button
            PublicProfileHeader(
                onNavigateBack = onNavigateBack,
                username = uiState.user?.userName ?: "Profile"
            )

            when {
                uiState.isLoading && !isRefreshing -> {
                    LoadingState()
                }
                uiState.error != null && !isRefreshing -> {
                    ErrorState(
                        error = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.loadUserProfile(userId) }
                    )
                }
                uiState.user != null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .pullRefresh(pullRefreshState)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(contentAlpha)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Profile header section
                            ProfileHeaderSection(user = uiState.user!!)

                            // Stats card - prominent placement
                            EnhancedStatsCard(
                                flagCount = uiState.flags.size,
                                currentStreak = uiState.userStats?.currentStreak ?: 0,
                                longestStreak = uiState.userStats?.longestStreak ?: 0,
                                badgesEarned = uiState.badgesEarned
                            )

                            // Bio section (if exists)
                            if (uiState.user!!.bio.isNullOrEmpty()) {
                                EmptyBioSection(username = uiState.user!!.userName ?: "This explorer")
                            } else {
                                BioSection(bio = uiState.user!!.bio!!)
                            }

                            // Friend request button
                            FriendRequestButton(
                                status = uiState.friendRequestStatus,
                                onSendRequest = {
                                    // Provide haptic feedback on press
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.sendFriendRequest(userId)
                                },
                                onCancelRequest = { requestId ->
                                    // Provide haptic feedback on press
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.cancelFriendRequest(requestId)
                                }
                            )

                            // Recent explorations or empty state
                            if (uiState.flags.isEmpty()) {
                                EmptyFlagsSection(
                                    username = uiState.user!!.userName?.uppercase() ?: "THIS EXPLORER",
                                    friendRequestStatus = uiState.friendRequestStatus
                                )
                            } else {
                                RecentExplorationsSection(
                                    flags = uiState.flags.take(5),
                                    displayNames = uiState.flagDisplayNames,
                                    locations = uiState.flagLocations,
                                    onFlagClick = { flag ->
                                        // Open location in Google Maps
                                        uiState.flagLocations[flag.locationId]?.let { (lat, lng) ->
                                            openLocationInGoogleMaps(
                                                context = context,
                                                latitude = lat,
                                                longitude = lng,
                                                locationName = uiState.flagDisplayNames[flag.locationId] ?: "Location"
                                            )
                                        }
                                    }
                                )
                            }

                            // Bottom spacer for comfortable scrolling
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Pull refresh indicator
                        PullRefreshIndicator(
                            refreshing = isRefreshing,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter),
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Celebration animation overlay
        FriendRequestSuccessAnimation(status = uiState.friendRequestStatus)
    }
}

/**
 * Header with back navigation and username
 * Provides spatial context and clear exit affordance
 */
@Composable
private fun PublicProfileHeader(
    onNavigateBack: () -> Unit,
    username: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        // Back button with touch target optimization
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title - centered
        Text(
            text = "PROFILE",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Profile picture and username section
 * Circular profile image creates visual anchor point with hero entrance animation
 */
@Composable
private fun ProfileHeaderSection(user: User) {
    // Separate animation states for avatar and content
    var avatarVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        avatarVisible = true
        kotlinx.coroutines.delay(100) // Content appears slightly after avatar
        contentVisible = true
    }

    // Avatar entrance animations
    val avatarScale by animateFloatAsState(
        targetValue = if (avatarVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatarScale"
    )

    val avatarRotation by animateFloatAsState(
        targetValue = if (avatarVisible) 0f else -15f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatarRotation"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile picture with decorative frame and hero animation
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .graphicsLayer {
                    scaleX = avatarScale
                    scaleY = avatarScale
                    rotationZ = avatarRotation
                },
            contentAlignment = Alignment.Center
        ) {
            // Outer frame (dark brown)
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(6.dp)
            ) {
                // Inner frame (orange accent)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(4.dp)
                ) {
                    // Profile image or initials
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        // Display user initials
                        Text(
                            text = ProfileUtils.getUserInitials(user.userName ?: "?"),
                            color = MaterialTheme.colorScheme.surface,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Username and divider with delayed entrance animation
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(400)) +
                    slideInVertically(
                        initialOffsetY = { -20 },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Username with decorative styling
                Text(
                    text = user.userName?.uppercase() ?: "EXPLORER",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                // Decorative divider
                Row(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
            }
        }
    }
}

/**
 * Enhanced stats card with 2x2 grid layout displaying key user statistics
 * Features staggered entrance animation for visual interest
 */
@Composable
private fun EnhancedStatsCard(
    flagCount: Int,
    currentStreak: Int,
    longestStreak: Int,
    badgesEarned: Int
) {
    // Overall card entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "statsCardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top row: Flags and Current Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    value = flagCount,
                    label = "FLAGS",
                    icon = Icons.Default.Flag,
                    delay = 0,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    value = currentStreak,
                    label = "STREAK",
                    icon = Icons.Default.LocalFireDepartment,
                    delay = 80,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottom row: Longest Streak and Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    value = longestStreak,
                    label = "BEST STREAK",
                    icon = Icons.Default.EmojiEvents,
                    delay = 160,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    value = badgesEarned,
                    label = "BADGES",
                    icon = Icons.Default.Stars,
                    delay = 240,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual stat item with icon, value, and label
 * Includes staggered entrance animation
 */
@Composable
private fun StatItem(
    value: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delay: Long,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "statAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "statScale"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )

        // Value
        Text(
            text = value.toString(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        // Label
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Bio section with comfortable reading layout
 * Multi-line text with optimal line height for readability
 */
@Composable
private fun BioSection(bio: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "ABOUT",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = bio,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * Empty bio section - encourages user to add a bio
 * Friendly prompt to enhance profile completeness
 */
@Composable
private fun EmptyBioSection(username: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$username hasn't added a bio yet :( \n\nSeems like you will have to talk irl to get to know each other (°□°˶)! \n\nOr you can go make some flags together ;)",
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

/**
 * Empty flags section - shows different messages based on friendship status
 * - Not friends: Privacy message (flags are private)
 * - Friends: User hasn't explored yet
 */
@Composable
private fun EmptyFlagsSection(
    username: String,
    friendRequestStatus: FriendRequestStatus
) {
    // Determine message and icon based on friendship status
    val (mainMessage, subMessage, showLockIcon) = when (friendRequestStatus) {
        is FriendRequestStatus.Accepted -> {
            // Already friends - they just don't have flags
            Triple(
                "$username HASN'T STARTED EXPLORING YET!!!",
                "Invite them to discover new places together!",
                false
            )
        }
        else -> {
            // Not friends - flags are private
            Triple(
                "🔒 EXPLORATIONS ARE PRIVATE",
                "Become friends with $username to see where they've been exploring!",
                true
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Lock icon for privacy state
        if (showLockIcon) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp)
            )
        }

        // Main message
        Text(
            text = mainMessage,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )

        // Sub message
        Text(
            text = subMessage,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

/**
 * Friend request button with status-aware design
 * Provides clear affordance and appropriate visual feedback
 * Pending state shows success confirmation with delayed cancel option
 */
@Composable
private fun FriendRequestButton(
    status: FriendRequestStatus,
    onSendRequest: () -> Unit,
    onCancelRequest: (Int) -> Unit
) {
    // Track if cancel option should be shown (5-second delay)
    var showCancelOption by remember { mutableStateOf(false) }

    // Reset cancel option visibility when status changes
    LaunchedEffect(status) {
        if (status is FriendRequestStatus.Pending) {
            showCancelOption = false
            kotlinx.coroutines.delay(5000) // 5-second delay
            showCancelOption = true
        } else {
            showCancelOption = false
        }
    }

    // Button configuration based on status
    val config = when (status) {
        is FriendRequestStatus.NotSent -> {
            ButtonConfig(
                text = "SEND FRIEND REQUEST",
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                icon = Icons.Default.PersonAdd,
                enabled = true
            )
        }
        is FriendRequestStatus.Sending -> {
            ButtonConfig(
                text = "SENDING...",
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                contentColor = MaterialTheme.colorScheme.onSecondary,
                icon = Icons.Default.PersonAdd,
                enabled = false
            )
        }
        is FriendRequestStatus.Pending -> {
            ButtonConfig(
                text = "REQUEST SENT ✓",
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f), // Success green tint
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = Icons.Default.CheckCircle,
                enabled = false
            )
        }
        is FriendRequestStatus.Cancelling -> {
            ButtonConfig(
                text = "CANCELLING...",
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = Icons.Default.Close,
                enabled = false
            )
        }
        is FriendRequestStatus.Accepted -> {
            ButtonConfig(
                text = "FRIENDS",
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                icon = Icons.Default.Check,
                enabled = false
            )
        }
    }

    // Subtle press animation
    var isPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Main friend request button
        Button(
            onClick = {
                isPressed = true
                when (status) {
                    is FriendRequestStatus.NotSent -> onSendRequest()
                    else -> {} // Pending state button is disabled
                }
            },
            enabled = config.enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = config.containerColor,
                contentColor = config.contentColor,
                disabledContainerColor = config.containerColor,
                disabledContentColor = config.contentColor
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = config.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Cancel option (appears after 5 seconds in Pending state)
        if (status is FriendRequestStatus.Pending && showCancelOption) {
            TextButton(
                onClick = { onCancelRequest(status.requestId) },
                modifier = Modifier.alpha(0.7f)
            ) {
                Text(
                    text = "Cancel request",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

/**
 * Recent explorations list
 * Displays user's recent flags in a clean, scannable format
 */
@Composable
private fun RecentExplorationsSection(
    flags: List<Flag>,
    displayNames: Map<String, String>,
    locations: Map<String, Pair<Double, Double>>,
    onFlagClick: (Flag) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "RECENT EXPLORATIONS",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Flags list with staggered animation
        flags.forEachIndexed { index, flag ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(index * 50L)
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(300)) +
                        slideInHorizontally(
                            initialOffsetX = { 20 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
            ) {
                FlagItem(
                    flag = flag,
                    displayName = displayNames[flag.locationId],
                    onClick = { onFlagClick(flag) }
                )
            }
        }
    }
}

/**
 * Individual flag item card
 * Clean design with location name and date
 */
@Composable
private fun FlagItem(
    flag: Flag,
    displayName: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Location pin indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Location info
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = displayName ?: "Unknown Location",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = ProfileUtils.formatDate(flag.dateTaken),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Subtle arrow indicator
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Loading state with rotating compass animation
 * More thematic than generic spinner for an exploration app
 */
@Composable
private fun LoadingState() {
    // Infinite rotation animation for compass
    val infiniteTransition = rememberInfiniteTransition(label = "compass")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "compassRotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rotating compass icon
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "Loading",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            )
            Text(
                text = "Loading profile...",
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Error state with retry option
 */
@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("RETRY", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Celebration animation overlay for successful friend request
 * Displays a green checkmark with "Friend request sent!" message
 */
@Composable
private fun FriendRequestSuccessAnimation(status: FriendRequestStatus) {
    // Track previous status to detect when request is successfully sent
    var previousStatus by remember { mutableStateOf<FriendRequestStatus>(FriendRequestStatus.NotSent) }
    var showCelebration by remember { mutableStateOf(false) }

    // Detect transition from Sending to Pending (successful send)
    LaunchedEffect(status) {
        if (previousStatus is FriendRequestStatus.Sending && status is FriendRequestStatus.Pending) {
            showCelebration = true
            kotlinx.coroutines.delay(2500) // Show for 2.5 seconds
            showCelebration = false
        }
        previousStatus = status
    }

    // Celebration animation
    AnimatedVisibility(
        visible = showCelebration,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Dark overlay with success card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                // Success card
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Green checkmark with animation
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    // Success message
                    Text(
                        text = "Friend request sent!",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "You'll be notified when they accept",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ============================================================================
// Helper Data Classes and Utility Functions
// ============================================================================

/**
 * Configuration for friend request button states.
 * Encapsulates all visual properties based on the current friend request status.
 */
private data class ButtonConfig(
    val text: String,
    val containerColor: Color,
    val contentColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val enabled: Boolean
)

/**
 * Utility functions for profile display formatting.
 */
private object ProfileUtils {
    /**
     * Extract user initials from username.
     * Returns first letter or first two letters capitalized.
     *
     * Examples:
     * - "John Doe" -> "JD"
     * - "Alice" -> "AL"
     * - "X" -> "X"
     */
    fun getUserInitials(username: String): String {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) return "?"

        val parts = trimmed.split(" ")
        return when {
            parts.size >= 2 -> {
                "${parts[0].first()}${parts[1].first()}".uppercase()
            }
            trimmed.length >= 2 -> {
                trimmed.substring(0, 2).uppercase()
            }
            else -> {
                trimmed.first().uppercase()
            }
        }
    }

    /**
     * Format ISO 8601 date string to human-readable format.
     * Input: "yyyy-MM-dd'T'HH:mm:ss"
     * Output: "MMM dd, yyyy" (e.g., "Jan 15, 2024")
     */
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
