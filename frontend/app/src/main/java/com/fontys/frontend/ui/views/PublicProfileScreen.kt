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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Flag
import com.fontys.frontend.data.models.User
import com.fontys.frontend.ui.theme.ProfileColors
import com.fontys.frontend.ui.viewmodels.FriendRequestStatus
import com.fontys.frontend.ui.viewmodels.PublicProfileViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.Intent
import android.net.Uri

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
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($locationName)")
    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    // Check if Google Maps is installed
    if (mapsIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapsIntent)
    } else {
        // Fallback to browser if Google Maps is not installed
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://maps.google.com/?q=$latitude,$longitude")
        )
        context.startActivity(browserIntent)
    }
}

/**
 * Public Profile Screen - Minimalistic design inspired by AllTrails
 *
 * Features conscious delight factors:
 * - Clean visual hierarchy with strategic use of space
 * - Prominent stats display for exploration count
 * - Clear call-to-action buttons with appropriate visual weight
 *
 * Features subconscious comfort factors:
 * - Smooth animations with natural easing curves (300ms standard)
 * - Gestalt principles for natural grouping
 * - Consistent spacing system (8dp grid)
 * - Reduced cognitive load through progressive disclosure
 */
@Composable
fun PublicProfileScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToMap: ((Double, Double) -> Unit)? = null,
    viewModel: PublicProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val flags by viewModel.flags.collectAsState()
    val flagDisplayNames by viewModel.flagDisplayNames.collectAsState()
    val flagLocations by viewModel.flagLocations.collectAsState()
    val friendRequestStatus by viewModel.friendRequestStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load profile on composition
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    // Animated content visibility
    val contentAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileColors.Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with back button
            PublicProfileHeader(
                onNavigateBack = onNavigateBack,
                username = user?.userName ?: "Profile"
            )

            when {
                isLoading -> {
                    LoadingState()
                }
                error != null -> {
                    ErrorState(
                        error = error ?: "Unknown error",
                        onRetry = { viewModel.loadUserProfile(userId) }
                    )
                }
                user != null -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(contentAlpha)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile header section
                        ProfileHeaderSection(user = user!!)

                        // Stats card - prominent placement
                        StatsCard(flagCount = flags.size)

                        // Bio section (if exists)
                        if (user!!.bio.isNullOrEmpty()) {
                            EmptyBioSection(username = user!!.userName ?: "This explorer")
                        } else {
                            BioSection(bio = user!!.bio!!)
                        }

                        // Friend request button
                        FriendRequestButton(
                            status = friendRequestStatus,
                            onSendRequest = { viewModel.sendFriendRequest(userId) }
                        )

                        // Recent explorations or empty state
                        if (flags.isEmpty()) {
                            EmptyFlagsSection(
                                username = user!!.userName?.uppercase() ?: "THIS EXPLORER",
                                friendRequestStatus = friendRequestStatus
                            )
                        } else {
                            RecentExplorationsSection(
                                flags = flags.take(5),
                                displayNames = flagDisplayNames,
                                locations = flagLocations,
                                onFlagClick = { flag ->
                                    // Open location in Google Maps
                                    flagLocations[flag.locationId]?.let { (lat, lng) ->
                                        openLocationInGoogleMaps(
                                            context = context,
                                            latitude = lat,
                                            longitude = lng,
                                            locationName = flagDisplayNames[flag.locationId] ?: "Location"
                                        )
                                    }
                                }
                            )
                        }

                        // Bottom spacer for comfortable scrolling
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
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
            .background(ProfileColors.Primary)
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
                tint = ProfileColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title - centered
        Text(
            text = "PROFILE",
            color = ProfileColors.TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Profile picture and username section
 * Circular profile image creates visual anchor point
 */
@Composable
private fun ProfileHeaderSection(user: User) {
    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) +
                slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile picture with decorative frame
            Box(
                modifier = Modifier.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer frame (dark brown)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(ProfileColors.Primary)
                        .padding(6.dp)
                ) {
                    // Inner frame (orange accent)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(ProfileColors.Accent)
                            .padding(4.dp)
                    ) {
                        // Profile image or initials
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(ProfileColors.Container),
                            contentAlignment = Alignment.Center
                        ) {
                            // Display user initials
                            Text(
                                text = ProfileUtils.getUserInitials(user.userName ?: "?"),
                                color = ProfileColors.Primary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Username with decorative styling
            Text(
                text = user.userName?.uppercase() ?: "EXPLORER",
                color = ProfileColors.Primary,
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
                        .background(ProfileColors.Accent)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ProfileColors.Primary)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(ProfileColors.Accent)
                )
            }
        }
    }
}

/**
 * Prominent stats card showing exploration count
 * Creates achievement-oriented visual anchor
 */
@Composable
private fun StatsCard(flagCount: Int) {
    // Subtle scale animation on appearance
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
        label = "statsScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(ProfileColors.Container)
            .border(2.dp, ProfileColors.Border, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ProfileColors.Accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = ProfileColors.Accent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Stats text
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = flagCount.toString(),
                    color = ProfileColors.Primary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "FLAGS",
                    color = ProfileColors.Primary.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
            }
        }
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
            .background(ProfileColors.Container)
            .border(1.dp, ProfileColors.Border.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
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
                tint = ProfileColors.Accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "ABOUT",
                color = ProfileColors.Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = bio,
            color = ProfileColors.Primary.copy(alpha = 0.8f),
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
            .background(ProfileColors.Container)
            .border(1.dp, ProfileColors.Border.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$username hasn't added a bio yet :( \n\nSeems like you will have to talk irl to get to know each other (°□°˶)! \n\nOr you can go make some flags together ;)",
            color = ProfileColors.Primary.copy(alpha = 0.6f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

/**
 * Empty flags section - encourages user to explore and add flags
 * Friendly prompt to motivate exploration
 */
@Composable
private fun EmptyFlagsSection(
    username: String,
    friendRequestStatus: FriendRequestStatus
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ProfileColors.Container)
            .border(1.dp, ProfileColors.Border.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main message
        Text(
            text = "$username HASN'T STARTED EXPLORING YET!!!",
            color = ProfileColors.Primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )

        // Call to action
        Text(
            text = "Send him/her a friend request to spark the interest in exploring otherwise - cooked",
            color = ProfileColors.Primary.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Friend request button with status-aware design
 * Provides clear affordance and appropriate visual feedback
 */
@Composable
private fun FriendRequestButton(
    status: FriendRequestStatus,
    onSendRequest: () -> Unit
) {
    // Button configuration based on status
    val (buttonText, buttonColor, contentColor, icon, enabled) = when (status) {
        is FriendRequestStatus.NotSent -> {
            ButtonConfig(
                text = "SEND FRIEND REQUEST",
                containerColor = ProfileColors.Accent,
                contentColor = ProfileColors.Primary,
                icon = Icons.Default.PersonAdd,
                enabled = true
            )
        }
        is FriendRequestStatus.Sending -> {
            ButtonConfig(
                text = "SENDING...",
                containerColor = ProfileColors.Accent.copy(alpha = 0.6f),
                contentColor = ProfileColors.Primary,
                icon = Icons.Default.PersonAdd,
                enabled = false
            )
        }
        is FriendRequestStatus.Pending -> {
            ButtonConfig(
                text = "REQUEST PENDING",
                containerColor = ProfileColors.Container,
                contentColor = ProfileColors.Primary,
                icon = Icons.Default.Schedule,
                enabled = false
            )
        }
        is FriendRequestStatus.Accepted -> {
            ButtonConfig(
                text = "FRIENDS",
                containerColor = ProfileColors.Primary,
                contentColor = ProfileColors.TextSecondary,
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

    Button(
        onClick = {
            isPressed = true
            onSendRequest()
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = contentColor,
            disabledContainerColor = buttonColor,
            disabledContentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 24.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, ProfileColors.Border)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = buttonText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
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
                tint = ProfileColors.Accent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "RECENT EXPLORATIONS",
                color = ProfileColors.Primary,
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
            .background(ProfileColors.Container)
            .border(1.dp, ProfileColors.Border.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
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
                    .background(ProfileColors.Accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = ProfileColors.Accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Location info
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = displayName ?: "Unknown Location",
                    color = ProfileColors.Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = ProfileUtils.formatDate(flag.dateTaken),
                    color = ProfileColors.Primary.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Subtle arrow indicator
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = ProfileColors.Primary.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Loading state with centered spinner
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = ProfileColors.Accent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading profile...",
                color = ProfileColors.Primary.copy(alpha = 0.6f),
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
                tint = ProfileColors.Primary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = error,
                color = ProfileColors.Primary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileColors.Accent,
                    contentColor = ProfileColors.Primary
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, ProfileColors.Border, RoundedCornerShape(8.dp))
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
