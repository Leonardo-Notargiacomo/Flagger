package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.UserUpdate
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.components.*
import com.fontys.frontend.ui.viewmodels.ProfileViewModel
import com.fontys.frontend.ui.viewmodels.BadgeViewModel
import com.fontys.frontend.ui.viewmodels.FriendsViewModel
import com.fontys.frontend.ui.viewmodels.BadgeUiState
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userViewModel: ProfileViewModel = viewModel(),
    badgeViewModel: BadgeViewModel = viewModel(),
    friendsViewModel: FriendsViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()
    val successMessage by userViewModel.successMessage.collectAsState()

    val badgeUiState by badgeViewModel.uiState.collectAsState()
    val friendsUiState by friendsViewModel.uiState.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Load user data, badges, and friends once when screen opens
    LaunchedEffect(Unit) {
        userViewModel.getUser(UserRepository.userId.toString())
        badgeViewModel.loadUserBadges(UserRepository.userId)
        friendsViewModel.loadFriends()
    }

    // Calculate stats
    val badgeCount = when (val state = badgeUiState) {
        is BadgeUiState.Success -> state.badges.count { it.unlockedAt != null }
        else -> 0
    }
    val friendCount = friendsUiState.friends.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        when {
            isLoading -> {
                ProfileScreenSkeleton()
            }
            user != null -> {
                val userData = user!!

                if (!isEditing) {
                    // Populate edit fields when switching to edit mode
                    editUsername = userData.userName
                    editEmail = userData.email
                    editBio = userData.bio
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Success message
                    successMessage?.let { message ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
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
                                IconButton(onClick = { userViewModel.clearSuccessMessage() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss")
                                }
                            }
                        }
                    }

                    // Error message
                    error?.let { errorMsg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = errorMsg,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { userViewModel.clearError() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss")
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        ProfilePictureSection(
                        username = if (isEditing) editUsername else userData.userName,
                        isEditing = isEditing,
                        onImageEdit = { /* TODO */ }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Statistics Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Badges stat
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Badges",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = badgeCount.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "BADGES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }

                            // Friends stat
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Friends",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = friendCount.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "FRIENDS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditing) {
                        EditableAccountField(
                            label = "Username",
                            value = editUsername,
                            onValueChange = { editUsername = it }
                        )
                        EditableAccountField(
                            label = "Email",
                            value = editEmail,
                            onValueChange = { editEmail = it }
                        )
                        EditableAccountField(
                            label = "Bio",
                            value = editBio,
                            onValueChange = { editBio = it },
                            multiline = true
                        )
                    } else {
                        AccountField("Username", userData.userName)
                        AccountField("Email", userData.email)
                        AccountField("Bio", userData.bio, multiline = true)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isEditing) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val userUpdate = UserUpdate(
                                            id = 1,
                                            userName = editUsername,
                                            email = editEmail,
                                            bio = editBio,
                                            userImage = 0
                                        )
                                        userViewModel.updateUser(userData.id, userUpdate)
                                        isEditing = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp
                                )
                            ) {
                                Text("Save Changes", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 14.sp)
                            }

                            TextButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, fontSize = 14.sp)
                            }
                        } else {
                            Button(
                                onClick = { isEditing = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp
                                )
                            ) {
                                Text("Edit Profile", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}
