# Profile Viewing System Documentation

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Components Breakdown](#components-breakdown)
4. [Complete Flow Diagram](#complete-flow-diagram)
5. [Line-by-Line Code Explanation](#line-by-line-code-explanation)
6. [Data Models](#data-models)
7. [API Integration](#api-integration)
8. [Testing the System](#testing-the-system)

---

## Overview

The profile viewing system allows users to view other users' public profiles from the Friends screen. Users can see another user's stats, bio, recent explorations (flags), and send friend requests.

### Key Concepts:
- **Public Profile Screen**: A read-only view of another user's profile
- **User Stats**: Displays flag count, current streak, longest streak, and badges earned
- **Friend Requests**: Users can send, view status, or cancel friend requests
- **Recent Explorations**: Shows the most recent 5 flags (locations) the user has explored
- **Pull-to-Refresh**: Users can swipe down to refresh profile data
- **Haptic Feedback**: Provides tactile response when interacting with buttons

---

## System Architecture

```
FriendsScreen                    PublicProfileScreen              Backend API
     |                                   |                              |
     |  1. User clicks on friend        |                              |
     |     or search result              |                              |
     |---------------------------------->|                              |
     |                                   |                              |
     |                                   |  2. Load user profile        |
     |                                   |----------------------------->|
     |                                   |                              |
     |                                   |  3. GET /go-users/{id}       |
     |                                   |<-----------------------------|
     |                                   |                              |
     |                                   |  4. GET /friends/{id}/flags  |
     |                                   |----------------------------->|
     |                                   |<-----------------------------|
     |                                   |                              |
     |                                   |  5. GET /user-stats/{id}     |
     |                                   |----------------------------->|
     |                                   |<-----------------------------|
     |                                   |                              |
     |                                   |  6. GET /user-badges/{id}    |
     |                                   |----------------------------->|
     |                                   |<-----------------------------|
     |                                   |                              |
     |                                   |  7. Check friend status      |
     |                                   |     (sent requests, friends) |
     |                                   |----------------------------->|
     |                                   |<-----------------------------|
     |                                   |                              |
     |                                   |  8. Display profile          |
     |                                   |                              |
     |                                   | 9. User sends friend request |
     |                                   |                              |
     |                                   | 10. POST /friend-requests    |
     |                                   |----------------------------->|
     |                                   |<-----------------------------|
     |                                   |                              |
     |                                   | 11. Show success animation   |
```

### Components:

1. **PublicProfileScreen.kt** - The main UI screen for viewing public profiles
2. **PublicProfileViewModel.kt** - Manages profile data and business logic
3. **FriendsRepository.kt** - Handles API calls for friend-related operations
4. **FriendsViewModel.kt** - Manages friends list and search functionality
5. **FriendsApi.kt** - Retrofit interface defining API endpoints
6. **Data Models** - User, Flag, FriendRequest, UserStats, etc.

---

## Complete Flow Diagram

### Step 1: Navigating to a Public Profile

```
User on FriendsScreen
       |
       |---> User searches for a user
       |     (SearchTab in FriendsScreen)
       |
       |---> Search results displayed
       |     (SearchUserItem components)
       |
       |---> User clicks on a user
       |     [Currently: No navigation implemented in FriendsScreen.kt]
       |     [Expected: Navigate to PublicProfileScreen with userId]
       |
       v
PublicProfileScreen opens (on viewProfiles branch)
```

**Note**: As of the current implementation in the `interactiveNotificationRequest` branch, clicking on a friend or search result does NOT navigate to the PublicProfileScreen. The navigation implementation exists on the `viewProfiles` branch.

### Step 2: Loading Profile Data

```
PublicProfileScreen.onCreate()
       |
       v
viewModel.loadUserProfile(userId)
       |
       |---> Set isLoading = true
       |
       |---> GET /go-users/{userId}
       |     (Fetch user basic info: username, email, bio)
       |
       |---> GET /friends/{userId}/flags
       |     (Fetch user's exploration flags)
       |
       |---> Fetch place names for flags
       |     (Get display names and coordinates)
       |
       |---> GET /user-stats/{userId}
       |     (Fetch current streak, longest streak)
       |
       |---> GET /user-badges/{userId}
       |     (Fetch badges earned count)
       |
       |---> checkFriendRequestStatus(userId)
       |     |
       |     |---> GET /friend-requests/sent
       |     |     (Check if request already sent)
       |     |
       |     |---> GET /friends
       |     |     (Check if already friends)
       |     |
       |     |---> Set friendRequestStatus
       |
       |---> Set isLoading = false
       |
       v
Display profile with all data
```

### Step 3: Sending a Friend Request

```
User taps "SEND FRIEND REQUEST" button
       |
       v
Haptic feedback triggered
       |
       v
viewModel.sendFriendRequest(userId)
       |
       |---> Set friendRequestStatus = Sending
       |
       |---> POST /friend-requests
       |     Body: { "toUserId": userId }
       |
       |---> If success:
       |     |
       |     |---> Set friendRequestStatus = Pending(requestId)
       |     |
       |     |---> Show success animation
       |     |     (Green checkmark overlay for 2.5 seconds)
       |     |
       |     |---> Button changes to "REQUEST SENT ✓"
       |     |
       |     |---> After 5 seconds, show "Cancel request" option
       |
       |---> If error:
             |---> Set friendRequestStatus = NotSent
             |---> Display error message
```

### Step 4: Cancelling a Friend Request

```
User waits 5 seconds after sending request
       |
       v
"Cancel request" link appears
       |
       v
User taps "Cancel request"
       |
       v
Haptic feedback triggered
       |
       v
viewModel.cancelFriendRequest(requestId)
       |
       |---> Set friendRequestStatus = Cancelling(requestId)
       |
       |---> DELETE /friend-requests/{requestId}
       |
       |---> If success:
       |     |---> Set friendRequestStatus = NotSent
       |     |---> Button changes back to "SEND FRIEND REQUEST"
       |
       |---> If error:
             |---> Set friendRequestStatus = Pending(requestId)
             |---> Display error message
```

### Step 5: Opening a Location in Google Maps

```
User taps on a flag in "RECENT EXPLORATIONS"
       |
       v
onFlagClick(flag) triggered
       |
       v
Get flag coordinates from flagLocations map
       |
       v
openLocationInGoogleMaps(context, lat, lng, locationName)
       |
       |---> Create Intent with Google Maps URL
       |     Format: "https://www.google.com/maps/search/?api=1&query={name}&query_place_id={lat},{lng}"
       |
       |---> Launch Intent
       |     (Opens Google Maps app or browser)
```

---

## Line-by-Line Code Explanation

### 1. PublicProfileScreen.kt

**Purpose**: The main UI screen that displays a user's public profile.

#### Lines 1-52: Package, Imports, and Utility Functions

```kotlin
package com.fontys.frontend.ui.views
```
- Declares the package for the view

```kotlin
import androidx.compose.animation.*
import androidx.compose.animation.core.*
```
- Imports animation libraries for smooth transitions and effects

```kotlin
import androidx.compose.material.ExperimentalMaterialApi
```
- Imports experimental Material Design features (pull-to-refresh)

```kotlin
private fun openLocationInGoogleMaps(
    context: Context,
    latitude: Double,
    longitude: Double,
    locationName: String = "Location"
)
```
- **Purpose**: Opens a flag location in Google Maps
- Uses HTTPS URL scheme instead of `geo:` URI for better compatibility
- Includes location name to help Google Maps find the actual place

```kotlin
val encodedName = Uri.encode(locationName)
val uri = "https://www.google.com/maps/search/?api=1&query=$encodedName&query_place_id=$latitude,$longitude".toUri()
```
- Encodes the location name to handle special characters
- Creates a Google Maps search URL with both name and coordinates
- `api=1` specifies the Google Maps URL API version

```kotlin
val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
}
```
- Creates an Intent to view the location
- `FLAG_ACTIVITY_REORDER_TO_FRONT`: Brings existing Maps activity to front instead of creating a new one

---

#### Lines 85-157: Main PublicProfileScreen Composable

```kotlin
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PublicProfileScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToMap: ((Double, Double) -> Unit)? = null,
    viewModel: PublicProfileViewModel = viewModel()
)
```
- **Parameters:**
  - `userId`: The ID of the user whose profile to display
  - `onNavigateBack`: Callback to navigate back to the previous screen
  - `onNavigateToMap`: Optional callback to navigate to the map (currently unused)
  - `viewModel`: ViewModel instance for managing profile data

```kotlin
val context = LocalContext.current
val hapticFeedback = LocalHapticFeedback.current
```
- `context`: Android context for launching intents
- `hapticFeedback`: Provides tactile feedback for button presses

```kotlin
val user by viewModel.user.collectAsState()
val flags by viewModel.flags.collectAsState()
val flagDisplayNames by viewModel.flagDisplayNames.collectAsState()
val flagLocations by viewModel.flagLocations.collectAsState()
val userStats by viewModel.userStats.collectAsState()
val badgesEarned by viewModel.badgesEarned.collectAsState()
val friendRequestStatus by viewModel.friendRequestStatus.collectAsState()
val isLoading by viewModel.isLoading.collectAsState()
val error by viewModel.error.collectAsState()
```
- Observes state flows from the ViewModel
- `by collectAsState()`: Converts Flow to Compose State for automatic recomposition
- **State variables:**
  - `user`: User profile data (username, email, bio)
  - `flags`: List of user's exploration flags
  - `flagDisplayNames`: Map of locationId to human-readable place names
  - `flagLocations`: Map of locationId to (latitude, longitude) coordinates
  - `userStats`: Streak data (current, longest)
  - `badgesEarned`: Count of badges earned
  - `friendRequestStatus`: Current status of friend request (NotSent, Pending, Accepted, etc.)
  - `isLoading`: Whether data is currently being loaded
  - `error`: Error message if loading failed

```kotlin
var isRefreshing by remember { mutableStateOf(false) }
val pullRefreshState = rememberPullRefreshState(
    refreshing = isRefreshing,
    onRefresh = {
        isRefreshing = true
        viewModel.loadUserProfile(userId)
    }
)
```
- `isRefreshing`: Tracks pull-to-refresh state
- `pullRefreshState`: Material Design pull-to-refresh state
- `onRefresh`: Called when user pulls down to refresh

```kotlin
LaunchedEffect(userId) {
    viewModel.loadUserProfile(userId)
}
```
- `LaunchedEffect`: Runs when the composable is first created
- Loads the user's profile data when the screen opens

```kotlin
LaunchedEffect(isLoading) {
    if (!isLoading && isRefreshing) {
        isRefreshing = false
    }
}
```
- Stops the refreshing animation when loading completes
- Only stops if loading was triggered by pull-to-refresh

```kotlin
val contentAlpha by animateFloatAsState(
    targetValue = if (isLoading && !isRefreshing) 0f else 1f,
    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
    label = "contentAlpha"
)
```
- Animates content opacity during loading
- Fades out when loading (but not when refreshing)
- `FastOutSlowInEasing`: Starts fast, ends slow (natural motion)

---

#### Lines 159-183: Header and Loading States

```kotlin
PublicProfileHeader(
    onNavigateBack = onNavigateBack,
    username = user?.userName ?: "Profile"
)
```
- Displays the header with back button and username
- Falls back to "Profile" if username is not loaded yet

```kotlin
when {
    isLoading && !isRefreshing -> {
        LoadingState()
    }
    error != null && !isRefreshing -> {
        ErrorState(
            error = error ?: "Unknown error",
            onRetry = { viewModel.loadUserProfile(userId) }
        )
    }
    user != null -> {
        // Display profile content...
    }
}
```
- **Conditional rendering:**
  - Show loading spinner if loading (and not refreshing)
  - Show error message if error occurred (and not refreshing)
  - Show profile content if user data is loaded

---

#### Lines 185-235: Profile Content

```kotlin
Box(
    modifier = Modifier
        .weight(1f)
        .pullRefresh(pullRefreshState)
)
```
- Wraps the profile content in a Box with pull-to-refresh functionality
- `weight(1f)`: Takes all remaining vertical space

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .alpha(contentAlpha)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
)
```
- Main content column
- `alpha(contentAlpha)`: Animated opacity for loading transition
- `verticalScroll`: Makes the content scrollable
- `spacedBy(16.dp)`: Adds 16dp spacing between child components

```kotlin
ProfileHeaderSection(user = user!!)
```
- Displays the profile picture (or initials) and username
- The `!!` operator asserts that `user` is not null (safe because we checked above)

```kotlin
EnhancedStatsCard(
    flagCount = flags.size,
    currentStreak = userStats?.currentStreak ?: 0,
    longestStreak = userStats?.longestStreak ?: 0,
    badgesEarned = badgesEarned
)
```
- Displays the stats card with 2x2 grid layout
- `?:` (Elvis operator): Uses 0 as fallback if stats are null

```kotlin
if (user!!.bio.isNullOrEmpty()) {
    EmptyBioSection(username = user!!.userName ?: "This explorer")
} else {
    BioSection(bio = user!!.bio!!)
}
```
- Shows either the user's bio or a placeholder message
- Encourages interaction if bio is empty

```kotlin
FriendRequestButton(
    status = friendRequestStatus,
    onSendRequest = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.sendFriendRequest(userId)
    },
    onCancelRequest = { requestId ->
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.cancelFriendRequest(requestId)
    }
)
```
- Displays the friend request button
- `hapticFeedback.performHapticFeedback()`: Provides tactile feedback on button press
- `HapticFeedbackType.LongPress`: Stronger haptic feedback (like long-pressing)

```kotlin
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
```
- Shows recent explorations or an empty state message
- `take(5)`: Limits to the 5 most recent flags
- `onFlagClick`: Opens the location in Google Maps when tapped

---

#### Lines 243-270: PublicProfileHeader Composable

```kotlin
@Composable
private fun PublicProfileHeader(
    onNavigateBack: () -> Unit,
    username: String
)
```
- **Purpose**: Displays the header with back button and title
- Simple, clean design with centered title

```kotlin
IconButton(
    onClick = onNavigateBack,
    modifier = Modifier
        .size(40.dp)
        .align(Alignment.CenterStart)
)
```
- Back button aligned to the left
- 40dp size provides comfortable touch target (Android recommendation: minimum 48dp, but acceptable for icon buttons)

```kotlin
Icon(
    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
    contentDescription = "Go back",
    tint = ProfileColors.TextSecondary,
    modifier = Modifier.size(24.dp)
)
```
- `Icons.AutoMirrored.Filled.ArrowBack`: Automatically flips in RTL (right-to-left) languages
- 24dp icon inside 40dp touch target (16dp padding on each side)

```kotlin
Text(
    text = "PROFILE",
    color = ProfileColors.TextSecondary,
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 2.sp,
    modifier = Modifier.align(Alignment.Center)
)
```
- Centered title in all caps
- `letterSpacing = 2.sp`: Adds spacing between letters for aesthetic effect

---

#### Lines 272-380: ProfileHeaderSection with Hero Animation

```kotlin
@Composable
private fun ProfileHeaderSection(user: User) {
    var avatarVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        avatarVisible = true
        kotlinx.coroutines.delay(100)
        contentVisible = true
    }
```
- **Hero entrance animation**: Avatar appears first, then content
- `LaunchedEffect(Unit)`: Runs once when composable is created
- 100ms delay between avatar and content animations

```kotlin
val avatarScale by animateFloatAsState(
    targetValue = if (avatarVisible) 1f else 0.8f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    label = "avatarScale"
)
```
- Animates avatar from 80% to 100% scale
- `spring()`: Creates a natural, bouncy animation
- `DampingRatioMediumBouncy`: Slight overshoot effect (playful feel)

```kotlin
val avatarRotation by animateFloatAsState(
    targetValue = if (avatarVisible) 0f else -15f,
    animationSpec = spring(
        dampingRatio = 0.7f,
        stiffness = Spring.StiffnessMedium
    ),
    label = "avatarRotation"
)
```
- Rotates avatar from -15° to 0°
- Combined with scale creates a "swinging into place" effect

```kotlin
Box(
    modifier = Modifier
        .padding(vertical = 8.dp)
        .graphicsLayer {
            scaleX = avatarScale
            scaleY = avatarScale
            rotationZ = avatarRotation
        },
    contentAlignment = Alignment.Center
)
```
- `graphicsLayer`: Efficient hardware-accelerated animations
- Applies both scale and rotation transformations

```kotlin
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
            Text(
                text = ProfileUtils.getUserInitials(user.userName ?: "?"),
                color = ProfileColors.Primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```
- **Three-layer design**: Outer frame (dark) → Inner frame (orange) → Profile picture/initials
- Creates a decorative "compass" or "badge" effect
- `getUserInitials()`: Extracts first letters of username (e.g., "John Doe" → "JD")

```kotlin
AnimatedVisibility(
    visible = contentVisible,
    enter = fadeIn(animationSpec = tween(400)) +
            slideInVertically(
                initialOffsetY = { -20 },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
)
```
- Username and divider fade in and slide down
- `fadeIn + slideInVertically`: Combined animation effects
- `initialOffsetY = { -20 }`: Starts 20 pixels above final position

---

#### Lines 382-505: EnhancedStatsCard with 2x2 Grid Layout

```kotlin
@Composable
private fun EnhancedStatsCard(
    flagCount: Int,
    currentStreak: Int,
    longestStreak: Int,
    badgesEarned: Int
)
```
- **Purpose**: Displays key user statistics in a 2x2 grid
- Clean, scannable layout with icons and labels

```kotlin
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
```
- Card scales from 90% to 100% with a bouncy animation
- `StiffnessLow`: Slower, more gentle animation

```kotlin
Column(
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(value = flagCount, label = "FLAGS", icon = Icons.Default.Flag, delay = 0, modifier = Modifier.weight(1f))
        StatItem(value = currentStreak, label = "STREAK", icon = Icons.Default.LocalFireDepartment, delay = 80, modifier = Modifier.weight(1f))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(value = longestStreak, label = "BEST STREAK", icon = Icons.Default.EmojiEvents, delay = 160, modifier = Modifier.weight(1f))
        StatItem(value = badgesEarned, label = "BADGES", icon = Icons.Default.Stars, delay = 240, modifier = Modifier.weight(1f))
    }
}
```
- **2x2 Grid Layout:**
  - **Top Row**: FLAGS, STREAK
  - **Bottom Row**: BEST STREAK, BADGES
- `weight(1f)`: Each item takes equal width
- **Staggered delays**: 0ms, 80ms, 160ms, 240ms (creates cascading animation effect)

```kotlin
@Composable
private fun StatItem(
    value: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delay: Long,
    modifier: Modifier = Modifier
)
```
- **Individual stat item** with icon, value, and label

```kotlin
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
```
- Each stat item has its own entrance animation
- `delay`: Staggered delays create cascading effect
- Fades in and scales from 85% to 100%

---

#### Lines 507-680: Bio, Empty States, and Friend Request Button

```kotlin
@Composable
private fun BioSection(bio: String)
```
- Displays the user's bio text
- Multi-line text with optimal line height for readability (`lineHeight = 20.sp`)

```kotlin
@Composable
private fun EmptyBioSection(username: String)
```
- Shown when user hasn't added a bio
- Friendly, playful message encouraging real-life interaction
- Example: "seems like you will have to talk irl to get to know each other (°□°˶)!"

```kotlin
@Composable
private fun EmptyFlagsSection(
    username: String,
    friendRequestStatus: FriendRequestStatus
)
```
- Shown when user has no flags
- Encourages sending a friend request to motivate exploration

```kotlin
@Composable
private fun FriendRequestButton(
    status: FriendRequestStatus,
    onSendRequest: () -> Unit,
    onCancelRequest: (Int) -> Unit
)
```
- **Purpose**: Displays the friend request button with status-aware design
- Changes appearance and behavior based on current status

```kotlin
var showCancelOption by remember { mutableStateOf(false) }

LaunchedEffect(status) {
    if (status is FriendRequestStatus.Pending) {
        showCancelOption = false
        kotlinx.coroutines.delay(5000) // 5-second delay
        showCancelOption = true
    } else {
        showCancelOption = false
    }
}
```
- **Delayed cancel option**: After sending a request, user must wait 5 seconds before they can cancel
- Prevents accidental cancellations

```kotlin
val config = when (status) {
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
            text = "REQUEST SENT ✓",
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
            contentColor = ProfileColors.Primary,
            icon = Icons.Default.CheckCircle,
            enabled = false
        )
    }
    is FriendRequestStatus.Cancelling -> {
        ButtonConfig(
            text = "CANCELLING...",
            containerColor = ProfileColors.Container.copy(alpha = 0.6f),
            contentColor = ProfileColors.Primary,
            icon = Icons.Default.Close,
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
```
- **Status-based configuration**:
  - **NotSent**: Orange "SEND FRIEND REQUEST" button (enabled)
  - **Sending**: Slightly transparent orange "SENDING..." (disabled)
  - **Pending**: Light green "REQUEST SENT ✓" (disabled, shows success)
  - **Cancelling**: Gray "CANCELLING..." (disabled)
  - **Accepted**: Dark brown "FRIENDS" (disabled, permanent state)

```kotlin
var isPressed by remember { mutableStateOf(false) }
val buttonScale by animateFloatAsState(
    targetValue = if (isPressed) 0.97f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    ),
    label = "buttonScale"
)
```
- **Subtle press animation**: Button scales to 97% when pressed
- Creates a satisfying tactile feel

```kotlin
if (status is FriendRequestStatus.Pending && showCancelOption) {
    TextButton(
        onClick = { onCancelRequest(status.requestId) },
        modifier = Modifier.alpha(0.7f)
    ) {
        Text(
            text = "Cancel request",
            color = ProfileColors.Primary.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
```
- **Cancel option**: Appears 5 seconds after sending request
- Subtle design (low alpha) to not distract from success state

---

#### Lines 682-820: Recent Explorations and Flag Items

```kotlin
@Composable
private fun RecentExplorationsSection(
    flags: List<Flag>,
    displayNames: Map<String, String>,
    locations: Map<String, Pair<Double, Double>>,
    onFlagClick: (Flag) -> Unit
)
```
- **Purpose**: Displays a list of the user's recent flags
- Shows up to 5 most recent explorations

```kotlin
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
    )
```
- **Staggered entrance animation**: Each flag appears 50ms after the previous one
- Slides in from the right (20 pixels) and fades in
- Creates a smooth, cascading effect

```kotlin
@Composable
private fun FlagItem(
    flag: Flag,
    displayName: String?,
    onClick: () -> Unit
)
```
- **Individual flag item card**
- Clean design with location icon, name, and date

```kotlin
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
)
```
- Clickable card with subtle border
- `clickable { onClick() }`: Opens location in Google Maps when tapped

```kotlin
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
```
- **Location pin indicator**: Orange accent color with circular background
- Provides visual anchor and indicates this is a location

```kotlin
Text(
    text = displayName ?: "Unknown Location",
    color = ProfileColors.Primary,
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
)
```
- Displays the location name (e.g., "Central Park", "Eiffel Tower")
- `maxLines = 1`: Limits to one line
- `overflow = TextOverflow.Ellipsis`: Shows "..." if text is too long

```kotlin
Text(
    text = ProfileUtils.formatDate(flag.dateTaken),
    color = ProfileColors.Primary.copy(alpha = 0.6f),
    fontSize = 12.sp,
    fontWeight = FontWeight.Normal
)
```
- Displays when the flag was created (e.g., "Jan 15, 2024")

```kotlin
Icon(
    imageVector = Icons.Default.ChevronRight,
    contentDescription = null,
    tint = ProfileColors.Primary.copy(alpha = 0.3f),
    modifier = Modifier.size(20.dp)
)
```
- **Subtle arrow indicator**: Hints that the item is tappable
- Low alpha (30%) to not distract from content

---

#### Lines 822-960: Loading, Error, and Success Animation

```kotlin
@Composable
private fun LoadingState()
```
- **Purpose**: Displays a loading spinner when fetching profile data
- Uses a rotating compass icon (thematic for exploration app)

```kotlin
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
```
- **Infinite rotation animation**: Compass spins continuously
- 2000ms (2 seconds) per full rotation
- `LinearEasing`: Constant rotation speed

```kotlin
@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
)
```
- **Purpose**: Displays an error message with a retry button
- Shows error icon, error message, and "RETRY" button

```kotlin
@Composable
private fun FriendRequestSuccessAnimation(status: FriendRequestStatus)
```
- **Purpose**: Shows a celebration animation when friend request is successfully sent
- Full-screen overlay with green checkmark and success message

```kotlin
var previousStatus by remember { mutableStateOf<FriendRequestStatus>(FriendRequestStatus.NotSent) }
var showCelebration by remember { mutableStateOf(false) }

LaunchedEffect(status) {
    if (previousStatus is FriendRequestStatus.Sending && status is FriendRequestStatus.Pending) {
        showCelebration = true
        kotlinx.coroutines.delay(2500) // Show for 2.5 seconds
        showCelebration = false
    }
    previousStatus = status
}
```
- **Detects transition**: Sending → Pending (successful send)
- Shows animation for 2.5 seconds, then automatically dismisses
- Tracks previous status to detect state changes

```kotlin
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
)
```
- Fades in and scales from 80% to 100% with a bouncy effect
- Fades out after 2.5 seconds

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.4f)),
    contentAlignment = Alignment.Center
)
```
- Semi-transparent black overlay (40% opacity)
- Dims the background to focus attention on success message

```kotlin
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
```
- **Green checkmark**: 56dp icon inside 80dp circle
- `Color(0xFF4CAF50)`: Material Green 500 (success color)

```kotlin
Text(
    text = "Friend request sent!",
    color = ProfileColors.Primary,
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center
)

Text(
    text = "You'll be notified when they accept",
    color = ProfileColors.Primary.copy(alpha = 0.7f),
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    textAlign = TextAlign.Center
)
```
- Success message and secondary text
- Reassures user that they will be notified when the request is accepted

---

#### Lines 962-1050: Helper Functions and Utilities

```kotlin
private data class ButtonConfig(
    val text: String,
    val containerColor: Color,
    val contentColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val enabled: Boolean
)
```
- **Data class**: Encapsulates button configuration based on friend request status
- Makes the button state logic cleaner and more maintainable

```kotlin
private object ProfileUtils {
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
```
- **Purpose**: Extracts user initials from username
- **Examples:**
  - "John Doe" → "JD"
  - "Alice" → "AL"
  - "X" → "X"
  - "" → "?"

```kotlin
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
```
- **Purpose**: Formats ISO 8601 date strings to human-readable format
- **Input**: "2024-01-15T14:30:00"
- **Output**: "Jan 15, 2024"
- Falls back to original string if parsing fails

---

### 2. PublicProfileViewModel.kt

**Purpose**: Manages the business logic and state for the public profile screen.

#### Lines 1-40: Imports, State, and Setup

```kotlin
package com.fontys.frontend.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
```
- `AndroidViewModel`: ViewModel with Application context access

```kotlin
data class PublicProfileUiState(
    val user: User? = null,
    val flags: List<Flag> = emptyList(),
    val flagDisplayNames: Map<String, String> = emptyMap(),
    val flagLocations: Map<String, Pair<Double, Double>> = emptyMap(),
    val userStats: UserStats? = null,
    val badgesEarned: Int = 0,
    val friendRequestStatus: FriendRequestStatus = FriendRequestStatus.NotSent,
    val isLoading: Boolean = false,
    val error: String? = null
)
```
- **UI State data class**: Consolidates all profile-related state
- Single source of truth for the UI
- Makes it easier to update multiple properties atomically

```kotlin
class PublicProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val friendsRepository = FriendsRepository()
    private val mapRepository = MapRepository()
    private val badgeRepository = BadgeRepository()
```
- **Repository instances**: Handles API calls and data operations

```kotlin
    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState: StateFlow<PublicProfileUiState> = _uiState.asStateFlow()
```
- **State management**: MutableStateFlow (private) and StateFlow (public)
- `asStateFlow()`: Converts MutableStateFlow to read-only StateFlow

```kotlin
    // Legacy API compatibility - expose individual properties
    val user: StateFlow<User?> = MutableStateFlow<User?>(null).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.user }
        }
    }
```
- **Legacy API**: Exposes individual properties for backward compatibility
- Each property flow collects from the main `_uiState` flow
- This pattern allows gradual migration to the consolidated state model

---

#### Lines 85-165: loadUserProfile() Method

```kotlin
fun loadUserProfile(userId: Int) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
```
- Sets loading state to true and clears any previous errors
- `copy()`: Creates a new state object with updated properties (immutable pattern)

```kotlin
        try {
            val token = getAuthToken() ?: run {
                _uiState.value = _uiState.value.copy(
                    error = "No authentication token",
                    isLoading = false
                )
                return@launch
            }
```
- Gets authentication token from `UserRepository`
- Returns early if token is not available

```kotlin
            // Load user data
            val userResult = friendsRepository.getUserById(token, userId)
            if (userResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load user profile",
                    isLoading = false
                )
                return@launch
            }

            val user = userResult.getOrNull()
            _uiState.value = _uiState.value.copy(user = user)
```
- **Step 1**: Fetch user basic info (username, email, bio)
- `getUserById()`: Returns `Result<User>` (Kotlin's safe error handling)
- Updates state with user data or error message

```kotlin
            // Load user's flags
            val flagsResult = friendsRepository.getFriendFlags(token, userId)
            if (flagsResult.isSuccess) {
                val flagsList = flagsResult.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(flags = flagsList)

                // Fetch place names for flags
                if (flagsList.isNotEmpty()) {
                    fetchPlaceNames(flagsList)
                }
            }
```
- **Step 2**: Fetch user's exploration flags
- **Step 3**: Fetch place names for flags (if any flags exist)

```kotlin
            // Load user stats
            val statsResult = badgeRepository.getUserStats(userId)
            if (statsResult.isSuccess) {
                val stats = statsResult.getOrNull()
                _uiState.value = _uiState.value.copy(userStats = stats)
            }
```
- **Step 4**: Fetch user stats (current streak, longest streak)

```kotlin
            // Load badges earned count
            val badgesResult = badgeRepository.getUserBadges(userId)
            if (badgesResult.isSuccess) {
                val badgesResponse = badgesResult.getOrNull()
                _uiState.value = _uiState.value.copy(
                    badgesEarned = badgesResponse?.earnedBadges ?: 0
                )
            }
```
- **Step 5**: Fetch badges earned count

```kotlin
            // Check friend request status
            checkFriendRequestStatus(userId)

        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile", e)
            _uiState.value = _uiState.value.copy(
                error = e.localizedMessage ?: "Unknown error"
            )
        } finally {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
```
- **Step 6**: Check friend request status
- `finally`: Always sets loading to false, even if an error occurs

---

#### Lines 167-215: checkFriendRequestStatus() Method

```kotlin
private suspend fun checkFriendRequestStatus(userId: Int) {
    try {
        val token = getAuthToken() ?: return
```
- Gets authentication token, returns early if not available

```kotlin
        // Check sent requests
        val sentRequestsResult = friendsRepository.getSentRequests(token)
        if (sentRequestsResult.isSuccess) {
            val sentRequests = sentRequestsResult.getOrNull() ?: emptyList()
            val pendingRequest = sentRequests.find {
                it.toUserId == userId && it.status == "PENDING"
            }

            if (pendingRequest != null) {
                _uiState.value = _uiState.value.copy(
                    friendRequestStatus = FriendRequestStatus.Pending(pendingRequest.id ?: 0)
                )
                return
            }
        }
```
- **Check 1**: Look for existing pending sent requests to this user
- If found, set status to `Pending(requestId)` and return early

```kotlin
        // Check if already friends
        val friendsResult = friendsRepository.getFriends(token)
        if (friendsResult.isSuccess) {
            val friends = friendsResult.getOrNull() ?: emptyList()
            val isFriend = friends.any { it.friendId == userId }

            if (isFriend) {
                _uiState.value = _uiState.value.copy(
                    friendRequestStatus = FriendRequestStatus.Accepted
                )
                return
            }
        }
```
- **Check 2**: Look for existing friendship
- If found, set status to `Accepted` and return early

```kotlin
        // Not sent
        _uiState.value = _uiState.value.copy(
            friendRequestStatus = FriendRequestStatus.NotSent
        )
```
- **Default**: If no pending request and not friends, set status to `NotSent`

---

#### Lines 217-260: sendFriendRequest() and cancelFriendRequest() Methods

```kotlin
fun sendFriendRequest(userId: Int) {
    viewModelScope.launch {
        try {
            val token = getAuthToken() ?: run {
                _uiState.value = _uiState.value.copy(error = "No authentication token")
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                friendRequestStatus = FriendRequestStatus.Sending
            )
```
- Sets status to `Sending` (shows loading state in UI)

```kotlin
            val result = friendsRepository.sendFriendRequest(token, userId)
            if (result.isSuccess) {
                val request = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    friendRequestStatus = FriendRequestStatus.Pending(request?.id ?: 0)
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to send friend request",
                    friendRequestStatus = FriendRequestStatus.NotSent
                )
            }
```
- Sends the friend request to the backend
- If successful, sets status to `Pending(requestId)`
- If failed, sets status back to `NotSent` and shows error

```kotlin
fun cancelFriendRequest(requestId: Int) {
    viewModelScope.launch {
        try {
            val token = getAuthToken() ?: run {
                _uiState.value = _uiState.value.copy(error = "No authentication token")
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                friendRequestStatus = FriendRequestStatus.Cancelling(requestId)
            )

            val result = friendsRepository.cancelFriendRequest(token, requestId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    friendRequestStatus = FriendRequestStatus.NotSent
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to cancel friend request",
                    friendRequestStatus = FriendRequestStatus.Pending(requestId)
                )
            }
```
- Cancels the friend request
- If successful, sets status back to `NotSent`
- If failed, keeps status as `Pending(requestId)` and shows error

---

#### Lines 262-295: fetchPlaceNames() Method

```kotlin
private fun fetchPlaceNames(flags: List<Flag>) {
    viewModelScope.launch {
        try {
            val locationIds = flags.map { it.locationId }
            val result = mapRepository.getLatlngs(locationIds)
```
- Gets location IDs from flags
- Fetches place names and coordinates from map repository

```kotlin
            if (result.isSuccess) {
                val flagDisplayList = result.getOrNull() ?: emptyList()
                val namesMap = mutableMapOf<String, String>()
                val locationsMap = mutableMapOf<String, Pair<Double, Double>>()

                flagDisplayList.forEach { flagDisplay ->
                    namesMap[flagDisplay.locationId] = flagDisplay.displayName
                    locationsMap[flagDisplay.locationId] = Pair(
                        flagDisplay.location.latitude,
                        flagDisplay.location.longitude
                    )
                }

                _uiState.value = _uiState.value.copy(
                    flagDisplayNames = namesMap,
                    flagLocations = locationsMap
                )
            }
```
- Builds two maps:
  - `namesMap`: locationId → display name (e.g., "central_park" → "Central Park")
  - `locationsMap`: locationId → (latitude, longitude)
- Updates state with both maps

```kotlin
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching place names", e)
            // Don't show error to user, just keep location IDs
        }
    }
}
```
- Silently fails if place names can't be fetched
- User will see location IDs instead of names (graceful degradation)

---

#### Lines 297-309: FriendRequestStatus Sealed Class

```kotlin
sealed class FriendRequestStatus {
    object NotSent : FriendRequestStatus()
    object Sending : FriendRequestStatus()
    data class Pending(val requestId: Int) : FriendRequestStatus()
    object Accepted : FriendRequestStatus()
    data class Cancelling(val requestId: Int) : FriendRequestStatus()
}
```
- **Sealed class**: Type-safe way to represent different states
- Each state is a distinct subclass
- **States:**
  - `NotSent`: No request has been sent (default state)
  - `Sending`: Request is currently being sent
  - `Pending(requestId)`: Request has been sent and is awaiting response
  - `Accepted`: Users are already friends
  - `Cancelling(requestId)`: Request is being cancelled

---

### 3. FriendsRepository.kt

**Purpose**: Handles all API calls related to friends, users, and friend requests.

#### Lines 1-28: User Operations

```kotlin
suspend fun getUserById(token: String, userId: Int): Result<User> {
    Log.d(TAG, "getUserById() called with userId: $userId")
    return try {
        val response = api.getUserById("Bearer $token", userId)
        Log.d(TAG, "getUserById() response code: ${response.code()}")
        Log.d(TAG, "getUserById() response body: ${response.body()}")
        handleResponse(response)
    } catch (e: Exception) {
        Log.e(TAG, "getUserById() error: ${e.message}", e)
        Result.failure(e)
    }
}
```
- **Purpose**: Fetches a user's profile by ID
- **Endpoint**: GET `/go-users/{id}`
- Returns `Result<User>` (Kotlin's Result type for safe error handling)
- Extensive logging for debugging

---

#### Lines 30-80: User Search

```kotlin
suspend fun searchUsers(token: String, query: String): Result<List<User>>
```
- **Purpose**: Searches for users by name or email
- **Endpoint**: GET `/go-users?filter={filterJson}`

```kotlin
val escapedQuery = query.replace("\\", "\\\\")
    .replace(".", "\\.")
    .replace("*", "\\*")
    // ... (escapes all regex special characters)
```
- Escapes regex special characters to treat query as literal string
- Prevents regex injection

```kotlin
val filterJson = JSONObject().apply {
    put("where", JSONObject().apply {
        put("or", org.json.JSONArray().apply {
            put(JSONObject().apply {
                put("userName", JSONObject().apply {
                    put("regexp", "/$escapedQuery/i")
                })
            })
            put(JSONObject().apply {
                put("email", JSONObject().apply {
                    put("regexp", "/$escapedQuery/i")
                })
            })
        })
    })
}.toString()
```
- **Filter format**: Loopback-style filter JSON
- **Logic**: Search in `userName` OR `email`
- **Regex**: `/$escapedQuery/i` (case-insensitive substring match)
- **Example**: Query "john" matches "John Doe", "johnny@example.com", etc.

---

#### Lines 82-155: Friend Requests Operations

```kotlin
suspend fun sendFriendRequest(token: String, toUserId: Int): Result<FriendRequest>
```
- **Purpose**: Sends a friend request
- **Endpoint**: POST `/friend-requests`
- **Body**: `{ "toUserId": userId }`

```kotlin
suspend fun getReceivedRequests(token: String): Result<List<FriendRequest>>
```
- **Purpose**: Gets received friend requests
- **Endpoint**: GET `/friend-requests/received`

```kotlin
suspend fun getSentRequests(token: String): Result<List<FriendRequest>>
```
- **Purpose**: Gets sent friend requests
- **Endpoint**: GET `/friend-requests/sent`

```kotlin
suspend fun acceptFriendRequest(token: String, requestId: Int): Result<AcceptFriendRequestResponse>
```
- **Purpose**: Accepts a friend request
- **Endpoint**: PATCH `/friend-requests/{id}/accept`

```kotlin
suspend fun rejectFriendRequest(token: String, requestId: Int): Result<RejectFriendRequestResponse>
```
- **Purpose**: Rejects a friend request
- **Endpoint**: PATCH `/friend-requests/{id}/reject`

```kotlin
suspend fun cancelFriendRequest(token: String, requestId: Int): Result<Unit>
```
- **Purpose**: Cancels a sent friend request
- **Endpoint**: DELETE `/friend-requests/{id}`

---

#### Lines 157-194: Friendships Operations

```kotlin
suspend fun getFriends(token: String): Result<List<FriendListItem>>
```
- **Purpose**: Gets the user's friends list
- **Endpoint**: GET `/friends`

```kotlin
suspend fun removeFriend(token: String, friendId: Int): Result<Unit>
```
- **Purpose**: Removes a friend
- **Endpoint**: DELETE `/friends/{friendId}`

```kotlin
suspend fun getFriendFlags(token: String, friendId: Int): Result<List<Flag>>
```
- **Purpose**: Gets a friend's exploration flags
- **Endpoint**: GET `/friends/{friendId}/flags`
- Used to display recent explorations on the public profile

---

#### Lines 196-204: Helper Function

```kotlin
private fun <T> handleResponse(response: Response<T>): Result<T> {
    return if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
    } else {
        Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
    }
}
```
- **Purpose**: Converts Retrofit Response to Kotlin Result
- Returns `Result.success()` if response is successful and body is not null
- Returns `Result.failure()` with error message if response failed

---

### 4. FriendsViewModel.kt

**Purpose**: Manages the UI state for the FriendsScreen (friends list, requests, search).

#### Lines 14-32: UI State and Enums

```kotlin
data class FriendsUiState(
    val friends: List<FriendListItem> = emptyList(),
    val receivedRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val searchResults: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val isLoadingFriends: Boolean = false,
    val isLoadingRequests: Boolean = false,
    val isSendingRequest: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
```
- **UI State**: Consolidates all friends-related state

```kotlin
enum class RelationshipStatus {
    NONE,           // No relationship
    FRIENDS,        // Already friends
    PENDING_SENT,   // Sent request waiting for response
    PENDING_RECEIVED // Received request waiting for your response
}
```
- **Relationship Status**: Used to display appropriate button in search results

---

#### Lines 53-83: searchUsers() Method

```kotlin
fun searchUsers(query: String) {
    Log.d(TAG, "searchUsers() called with query: '$query'")
    if (query.isBlank()) {
        Log.d(TAG, "searchUsers() query is blank, clearing results")
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
        return
    }
```
- Clears results if query is blank
- Early return to avoid unnecessary API calls

```kotlin
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isSearching = true, error = null)

        repository.searchUsers(authToken, query).fold(
            onSuccess = { users ->
                // Filter out current user from search results
                val filteredUsers = users.filter { it.id != currentUserId }
                Log.d(TAG, "searchUsers() success: found ${users.size} users, filtered to ${filteredUsers.size} (removed current user)")
                _uiState.value = _uiState.value.copy(
                    searchResults = filteredUsers,
                    isSearching = false
                )
            },
            onFailure = { error ->
                Log.e(TAG, "searchUsers() failed: ${error.message}", error)
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = error.message ?: "Failed to search users"
                )
            }
        )
    }
}
```
- Searches for users matching the query
- Filters out the current user from results (can't send request to yourself)

---

#### Lines 89-145: loadFriends() Method

```kotlin
fun loadFriends() {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoadingFriends = true, error = null)

        repository.getFriends(authToken).fold(
            onSuccess = { friends ->
                Log.d(TAG, "loadFriends() loaded ${friends.size} friends")

                // Identify friends with missing details
                val missingUserIds = friends
                    .filter { it.friendDetails == null }
                    .map { it.friendId }
                    .distinct()

                Log.d(TAG, "loadFriends() found ${missingUserIds.size} missing friend details: $missingUserIds")
```
- Loads the user's friends list
- Identifies friends with missing details (backend may not include user data)

```kotlin
                // Fetch missing friend details from API
                val fetchedUsers = mutableMapOf<Int, User>()
                missingUserIds.forEach { userId ->
                    repository.getUserById(authToken, userId).fold(
                        onSuccess = { user ->
                            fetchedUsers[user.id] = user
                            Log.d(TAG, "loadFriends() fetched user: ${user.userName} (id=${user.id})")
                        },
                        onFailure = { error ->
                            Log.e(TAG, "loadFriends() failed to fetch user $userId: ${error.message}")
                        }
                    )
                }
```
- Fetches missing user details one by one
- Builds a map of `userId → User`

```kotlin
                // Populate friendDetails with fetched data
                val enhancedFriends = friends.map { friend ->
                    if (friend.friendDetails == null && fetchedUsers.containsKey(friend.friendId)) {
                        friend.copy(friendDetails = fetchedUsers[friend.friendId])
                    } else {
                        friend
                    }
                }
```
- Enhances the friends list with fetched user details
- Uses `copy()` to create new immutable objects

---

#### Lines 409-428: getRelationshipStatus() Helper Method

```kotlin
fun getRelationshipStatus(userId: Int): RelationshipStatus {
    val state = _uiState.value

    // Check if already friends
    if (state.friends.any { it.friendId == userId }) {
        return RelationshipStatus.FRIENDS
    }

    // Check if there's a pending sent request
    if (state.sentRequests.any { it.toUserId == userId && it.status == "PENDING" }) {
        return RelationshipStatus.PENDING_SENT
    }

    // Check if there's a pending received request
    if (state.receivedRequests.any { it.fromUserId == userId && it.status == "PENDING" }) {
        return RelationshipStatus.PENDING_RECEIVED
    }

    return RelationshipStatus.NONE
}
```
- **Purpose**: Determines the relationship status with a specific user
- Used to display appropriate button in search results
- **Logic:**
  1. Check if already friends
  2. Check if sent request is pending
  3. Check if received request is pending
  4. Otherwise, no relationship

---

## Data Models

### User

```kotlin
data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("userName")
    val userName: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("bio")
    val bio: String?
)
```
- Represents a user's basic profile information
- `@SerializedName`: Maps JSON keys to Kotlin properties

### Flag

```kotlin
data class Flag(
    val id: Int,
    val userId: Int,
    val locationId: String,
    val photoCode: String?,
    val dateTaken: String // ISO 8601 format: "yyyy-MM-dd'T'HH:mm:ss"
)
```
- Represents an exploration flag (visited location)
- `locationId`: Unique identifier for the location
- `dateTaken`: Timestamp when the flag was created

### FriendRequest

```kotlin
data class FriendRequest(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("fromUserId")
    val fromUserId: Int,

    @SerializedName("toUserId")
    val toUserId: Int,

    @SerializedName("status")
    val status: String, // "PENDING", "ACCEPTED", "REJECTED"

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?,

    @SerializedName("fromUser")
    val fromUser: User? = null,

    @SerializedName("toUser")
    val toUser: User? = null
)
```
- Represents a friend request
- `fromUserId`: User who sent the request
- `toUserId`: User who received the request
- `status`: Current state ("PENDING", "ACCEPTED", "REJECTED")
- `fromUser`, `toUser`: Optional user details (if backend includes relations)

### UserStats

```kotlin
data class UserStats(
    val currentStreak: Int,
    val longestStreak: Int
)
```
- Represents user streak statistics

### FriendListItem

```kotlin
data class FriendListItem(
    val friendId: Int,
    val friendDetails: User? = null
)
```
- Represents a friend in the friends list
- `friendDetails`: Optional user details (may need to be fetched separately)

---

## API Integration

### Base URL

The app communicates with the backend API at:
```
https://i536374.hera.fhict.nl/api/
```

### Authentication

All API requests require authentication via Bearer token:
```kotlin
@Header("Authorization") token: String
```

Example:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

The token is stored in `UserRepository.token` after login.

---

### API Endpoints

#### User Endpoints

**Get User by ID**
```
GET /go-users/{id}
Authorization: Bearer {token}

Response: {
  "id": 123,
  "userName": "john_doe",
  "email": "john@example.com",
  "bio": "Love exploring!"
}
```

**Search Users**
```
GET /go-users?filter={filterJson}
Authorization: Bearer {token}

Filter JSON format:
{
  "where": {
    "or": [
      { "userName": { "regexp": "/query/i" } },
      { "email": { "regexp": "/query/i" } }
    ]
  }
}

Response: [
  {
    "id": 123,
    "userName": "john_doe",
    "email": "john@example.com",
    "bio": "Love exploring!"
  },
  ...
]
```

---

#### Friend Request Endpoints

**Send Friend Request**
```
POST /friend-requests
Authorization: Bearer {token}
Content-Type: application/json

Body:
{
  "toUserId": 456
}

Response: {
  "id": 789,
  "fromUserId": 123,
  "toUserId": 456,
  "status": "PENDING",
  "createdAt": "2024-01-15T14:30:00",
  "updatedAt": "2024-01-15T14:30:00"
}
```

**Get Received Requests**
```
GET /friend-requests/received
Authorization: Bearer {token}

Response: [
  {
    "id": 789,
    "fromUserId": 456,
    "toUserId": 123,
    "status": "PENDING",
    "createdAt": "2024-01-15T14:30:00",
    "fromUser": {
      "id": 456,
      "userName": "jane_doe",
      "email": "jane@example.com"
    }
  },
  ...
]
```

**Get Sent Requests**
```
GET /friend-requests/sent
Authorization: Bearer {token}

Response: [
  {
    "id": 790,
    "fromUserId": 123,
    "toUserId": 789,
    "status": "PENDING",
    "createdAt": "2024-01-15T15:00:00",
    "toUser": {
      "id": 789,
      "userName": "bob_smith",
      "email": "bob@example.com"
    }
  },
  ...
]
```

**Cancel Friend Request**
```
DELETE /friend-requests/{id}
Authorization: Bearer {token}

Response: 204 No Content
```

**Accept Friend Request**
```
PATCH /friend-requests/{id}/accept
Authorization: Bearer {token}

Response: {
  "message": "Friend request accepted",
  "friendRequest": {
    "id": 789,
    "fromUserId": 456,
    "toUserId": 123,
    "status": "ACCEPTED",
    "updatedAt": "2024-01-15T16:00:00"
  }
}
```

**Reject Friend Request**
```
PATCH /friend-requests/{id}/reject
Authorization: Bearer {token}

Response: {
  "message": "Friend request rejected"
}
```

---

#### Friendship Endpoints

**Get Friends**
```
GET /friends
Authorization: Bearer {token}

Response: [
  {
    "friendId": 456,
    "friendDetails": {
      "id": 456,
      "userName": "jane_doe",
      "email": "jane@example.com",
      "bio": "Explorer extraordinaire!"
    }
  },
  ...
]
```

**Remove Friend**
```
DELETE /friends/{friendId}
Authorization: Bearer {token}

Response: 204 No Content
```

**Get Friend's Flags**
```
GET /friends/{friendId}/flags
Authorization: Bearer {token}

Response: [
  {
    "id": 1001,
    "userId": 456,
    "locationId": "central_park_ny",
    "photoCode": null,
    "dateTaken": "2024-01-10T10:30:00"
  },
  ...
]
```

---

## Testing the System

### 1. Test Viewing a Public Profile

**Steps:**
1. Open the app and navigate to the Friends screen
2. Search for a user in the Search tab
3. Tap on a user in the search results
4. **Expected on viewProfiles branch**: PublicProfileScreen opens
5. Verify the following is displayed:
   - User's profile picture (or initials)
   - Username
   - Bio (or empty bio message)
   - Stats card with flags, streaks, and badges
   - Friend request button
   - Recent explorations (or empty state)

**Expected Result:** Profile loads correctly with all data displayed

**Current Status:** Navigation from FriendsScreen to PublicProfileScreen is not implemented in the `interactiveNotificationRequest` branch. This functionality exists in the `viewProfiles` branch.

---

### 2. Test Sending a Friend Request

**Steps:**
1. Open a user's public profile (user you're not friends with)
2. Verify the button says "SEND FRIEND REQUEST" (orange)
3. Tap the button
4. Verify haptic feedback occurs
5. Verify the button changes to "SENDING..."
6. Wait for the request to complete
7. Verify:
   - Success animation appears (green checkmark overlay)
   - Animation shows for 2.5 seconds
   - Button changes to "REQUEST SENT ✓" (light green)
   - Button is disabled (not clickable)

**Expected Result:**
- Request is sent successfully
- UI updates to show pending state
- Success animation appears

---

### 3. Test Cancelling a Friend Request

**Steps:**
1. Send a friend request (follow steps above)
2. Wait 5 seconds
3. Verify "Cancel request" link appears below the button
4. Tap "Cancel request"
5. Verify haptic feedback occurs
6. Verify the button changes to "CANCELLING..."
7. Wait for cancellation to complete
8. Verify the button changes back to "SEND FRIEND REQUEST" (orange)

**Expected Result:**
- Request is cancelled successfully
- Button returns to initial state

---

### 4. Test Opening a Location in Google Maps

**Steps:**
1. Open a user's public profile (user with at least one flag)
2. Scroll to "RECENT EXPLORATIONS" section
3. Tap on a flag item
4. Verify Google Maps opens (or browser if Maps not installed)
5. Verify the location is displayed correctly

**Expected Result:**
- Google Maps opens with the correct location
- Location name is displayed (if available)

---

### 5. Test Pull-to-Refresh

**Steps:**
1. Open a user's public profile
2. Pull down on the screen to refresh
3. Verify the refresh indicator appears
4. Wait for refresh to complete
5. Verify the refresh indicator disappears
6. Verify the profile data is reloaded

**Expected Result:**
- Refresh indicator appears and animates
- Profile data is reloaded
- Refresh indicator dismisses when loading completes

---

### 6. Test Loading States

**Steps:**
1. Open the app in airplane mode (or with slow network)
2. Navigate to a user's public profile
3. Verify loading spinner appears (rotating compass icon)
4. Verify "Loading profile..." text is displayed
5. Turn on network
6. Verify loading spinner disappears
7. Verify profile content appears

**Expected Result:**
- Loading state displays correctly
- Smooth transition to loaded state

---

### 7. Test Error States

**Steps:**
1. Open the app in airplane mode
2. Navigate to a user's public profile
3. Wait for the request to timeout
4. Verify error message appears
5. Verify "RETRY" button is displayed
6. Turn on network
7. Tap "RETRY"
8. Verify profile loads successfully

**Expected Result:**
- Error state displays correctly
- Retry functionality works

---

### 8. Test Animations

**Steps:**
1. Open a user's public profile
2. Observe the following animations:
   - Profile picture scales and rotates into view
   - Username fades in and slides down
   - Stats card scales up with bounce
   - Individual stats fade in and scale (staggered)
   - Recent explorations slide in from right (staggered)

**Expected Result:**
- All animations play smoothly
- Animations feel natural and polished
- No janky or laggy animations

---

### 9. Test Relationship Status Display

**Test Case A: Not Friends**
1. Open profile of user you're not friends with
2. Verify button says "SEND FRIEND REQUEST" (orange)

**Test Case B: Pending Sent Request**
1. Send a friend request
2. Reopen the profile
3. Verify button says "REQUEST SENT ✓" (light green)
4. Verify "Cancel request" option appears after 5 seconds

**Test Case C: Already Friends**
1. Open profile of existing friend
2. Verify button says "FRIENDS" (dark brown)
3. Verify button is disabled (not clickable)

**Expected Result:**
- Correct button state displayed for each relationship status
- Button appearance matches the status

---

### 10. Test Edge Cases

**Test Case A: User with No Flags**
1. Open profile of user with 0 flags
2. Verify empty state message appears
3. Verify message encourages sending friend request

**Test Case B: User with No Bio**
1. Open profile of user with empty bio
2. Verify empty bio message appears
3. Verify message is friendly and playful

**Test Case C: User with Long Bio**
1. Open profile of user with long bio (multiple lines)
2. Verify bio displays correctly with proper line breaks
3. Verify scrolling works if bio is very long

**Test Case D: User with Special Characters in Name**
1. Open profile of user with special characters (e.g., "José Müller")
2. Verify username displays correctly
3. Verify initials are extracted correctly

**Expected Result:**
- All edge cases handled gracefully
- No crashes or UI issues

---

## Summary

The profile viewing system allows users to:
1. **View public profiles** from the Friends screen
2. **See user stats** (flags, streaks, badges) in a clean, scannable 2x2 grid
3. **Send friend requests** with haptic feedback and success animation
4. **Cancel friend requests** after a 5-second delay
5. **View recent explorations** and open locations in Google Maps
6. **Pull to refresh** to reload profile data
7. **Experience smooth animations** throughout the UI

The system is designed with:
- **Clean visual hierarchy** for easy scanning
- **Smooth animations** with natural easing curves
- **Haptic feedback** for tactile confirmation
- **Status-aware UI** that adapts to relationship status
- **Error handling** with retry functionality
- **Loading states** with themed animations

Future enhancements could include:
- **Profile pictures** (currently shows initials)
- **More detailed stats** (flags by country, badges list)
- **Activity feed** (recent flags with timestamps)
- **Mutual friends** display
- **Chat functionality** from profile screen
