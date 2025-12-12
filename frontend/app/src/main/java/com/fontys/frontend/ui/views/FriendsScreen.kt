package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fontys.frontend.common.PublicProfileView
import com.fontys.frontend.data.models.FriendListItem
import com.fontys.frontend.data.models.FriendRequest
import com.fontys.frontend.ui.viewmodels.FriendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController = rememberNavController(),
    viewModel: FriendsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadFriends()
        viewModel.loadReceivedRequests()
        viewModel.loadSentRequests()
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Clean minimal tabs
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
                            "Friends",
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
                            "Requests",
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
                            "Search",
                            fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            // Error message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }

            // Success message
            uiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearSuccessMessage() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> FriendsListTab(
                    friends = uiState.friends,
                    isLoading = uiState.isLoadingFriends,
                    onRemoveFriend = { viewModel.removeFriend(it) },
                    onViewProfile = { userId -> navController.navigate(PublicProfileView(userId)) }
                )
                1 -> FriendRequestsTab(
                    receivedRequests = uiState.receivedRequests,
                    sentRequests = uiState.sentRequests,
                    isLoading = uiState.isLoadingRequests,
                    onAccept = { viewModel.acceptFriendRequest(it) },
                    onReject = { viewModel.rejectFriendRequest(it) },
                    onCancel = { viewModel.cancelFriendRequest(it) }
                )
                2 -> SearchTab(
                    searchResults = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    onSearch = { viewModel.searchUsers(it) },
                    onSendFriendRequest = { viewModel.sendFriendRequest(it) },
                    onViewProfile = { userId -> navController.navigate(PublicProfileView(userId)) }
                )
            }
        }
    }
}

@Composable
fun FriendsListTab(
    friends: List<FriendListItem>,
    isLoading: Boolean,
    onRemoveFriend: (Int) -> Unit,
    onViewProfile: (Int) -> Unit
) {
    if (isLoading) {
        com.fontys.frontend.ui.components.FriendListSkeleton()
    } else if (friends.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your adventure is lonely!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add friends to share your journey and compete for badges",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends) { friend ->
                FriendItem(
                    friend = friend,
                    onRemove = { onRemoveFriend(friend.friendId) },
                    onViewProfile = { onViewProfile(friend.friendId) }
                )
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: FriendListItem,
    onRemove: () -> Unit,
    onViewProfile: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewProfile() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.friendDetails?.userName ?: "User #${friend.friendId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                friend.friendDetails?.email?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                friend.friendDetails?.bio?.let { bio ->
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(onClick = { showRemoveDialog = true }) {
                Icon(
                    Icons.Default.PersonRemove,
                    contentDescription = "Remove friend",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Friend") },
            text = { Text("Are you sure you want to remove ${friend.friendDetails?.userName ?: "this user"} from your friends?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        showRemoveDialog = false
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FriendRequestsTab(
    receivedRequests: List<FriendRequest>,
    sentRequests: List<FriendRequest>,
    isLoading: Boolean,
    onAccept: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onCancel: (Int) -> Unit
) {
    if (isLoading && receivedRequests.isEmpty() && sentRequests.isEmpty()) {
        com.fontys.frontend.ui.components.FriendListSkeleton()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Received Requests Section
        item {
            Text(
                text = "Received Requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (receivedRequests.isEmpty()) {
            item {
                Text(
                    text = "No pending friend requests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(receivedRequests) { request ->
                ReceivedRequestItem(
                    request = request,
                    onAccept = { request.id?.let { onAccept(it) } },
                    onReject = { request.id?.let { onReject(it) } }
                )
            }
        }

        // Sent Requests Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sent Requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (sentRequests.isEmpty()) {
            item {
                Text(
                    text = "No sent requests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(sentRequests) { request ->
                SentRequestItem(
                    request = request,
                    onCancel = { request.id?.let { onCancel(it) } }
                )
            }
        }
        }
    }
}

@Composable
fun ReceivedRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = request.fromUser?.userName ?: "User #${request.fromUserId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            request.fromUser?.email?.let { email ->
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Accept", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Reject", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SentRequestItem(
    request: FriendRequest,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.toUser?.userName ?: "User #${request.toUserId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                request.toUser?.email?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "Status: ${request.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = if (request.toUser?.email != null) 4.dp else 0.dp)
                )
            }

            if (request.status == "PENDING") {
                OutlinedButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun SearchTab(
    searchResults: List<com.fontys.frontend.data.models.User>,
    isSearching: Boolean,
    onSearch: (String) -> Unit,
    onSendFriendRequest: (Int) -> Unit,
    onViewProfile: (Int) -> Unit,
    viewModel: FriendsViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearch(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search users by username (min 3 characters)...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        onSearch("")
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                cursorColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            singleLine = true
        )

        // Helper text for minimum search length
        if (searchQuery.isNotEmpty() && searchQuery.length < 3) {
            Text(
                text = "Enter at least 3 characters to search",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Results
        if (isSearching) {
            com.fontys.frontend.ui.components.FriendListSkeleton()
        } else if (searchQuery.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Search for users to add as friends",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No users found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { user ->
                    val relationshipStatus = viewModel.getRelationshipStatus(user.id)
                    SearchUserItem(
                        user = user,
                        relationshipStatus = relationshipStatus,
                        onSendRequest = { user.id?.let { onSendFriendRequest(it) } },
                        onViewProfile = { user.id?.let { onViewProfile(it) } }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchUserItem(
    user: com.fontys.frontend.data.models.User,
    relationshipStatus: com.fontys.frontend.ui.viewmodels.RelationshipStatus,
    onSendRequest: () -> Unit,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewProfile() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.userName ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                // Email removed for privacy - only show to actual friends
                user.bio?.let { bio ->
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Show different button based on relationship status
            when (relationshipStatus) {
                com.fontys.frontend.ui.viewmodels.RelationshipStatus.FRIENDS -> {
                    Button(
                        onClick = { /* Navigate to friend profile or manage */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Friends", fontWeight = FontWeight.Bold)
                    }
                }
                com.fontys.frontend.ui.viewmodels.RelationshipStatus.PENDING_SENT -> {
                    OutlinedButton(
                        onClick = { /* Maybe cancel request? */ },
                        enabled = false,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Pending", fontWeight = FontWeight.Bold)
                    }
                }
                com.fontys.frontend.ui.viewmodels.RelationshipStatus.PENDING_RECEIVED -> {
                    Button(
                        onClick = onSendRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Accept", fontWeight = FontWeight.Bold)
                    }
                }
                com.fontys.frontend.ui.viewmodels.RelationshipStatus.NONE -> {
                    Button(
                        onClick = onSendRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
