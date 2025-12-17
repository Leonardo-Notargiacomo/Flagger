package com.fontys.frontend.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fontys.frontend.common.AccountView
import com.fontys.frontend.data.UserUpdate
import com.fontys.frontend.data.models.FlagShowData
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.components.EditableAccountField
import com.fontys.frontend.ui.components.ProfileHeader
import com.fontys.frontend.ui.viewmodels.ProfileViewModel
import com.fontys.frontend.ui.viewmodels.SettingsMenu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    viewModel: ProfileViewModel,
    onAccountClick: (() -> Unit)? = null,
    onEditProfile: (() -> Unit)? = null,
    onDeleteAccount: (() -> Unit)? = null
) {
    // Local variables for the profile
    var pfp: String? by remember { mutableStateOf("") }
    var name: String? by remember { mutableStateOf("") }
    var bio: String? by remember { mutableStateOf("") }

    // Dynamic data
    val friends by viewModel.friendsNr.collectAsState()
    val friendsCount = friends ?: 0
    val postNr by viewModel.flagNrs.collectAsState()
    val postNames by viewModel.flagName.collectAsState()

    // ViewModel variables
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var changed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getUser(UserRepository.userId.toString())
    }

    // Main Screen Background (Warm Cream / Parchment)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .statusBarsPadding() // Added for safety
                .padding(16.dp) // Added outer padding so card doesn't touch edges
        ) {
            // Main Profile Card (Darker Cream "Paper" look)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(24.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Assuming ProfileHeader is custom, ensure it uses Theme colors internally
                ProfileHeader(onAccountClick = onAccountClick)

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    else -> {
                        LaunchedEffect(user) {
                            user?.let { uD ->
                                pfp = uD.userImage
                                name = uD.userName
                                bio = uD.bio
                            }
                        }
                        user?.let { uD ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TopAppBar(
                                    title = {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "Account",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.Transparent
                                    ),
                                    actions = {
                                        // Moved settings here for better UX, or keep empty if handled externally
                                        SettingsMenu(
                                            modifier = Modifier,
                                            onEditProfile = { onEditProfile?.invoke() },
                                            onDeleteAccount = { onDeleteAccount?.invoke() }
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Profile Picture Logic
                                pfp?.let {
                                    val image = remember(pfp) {
                                        viewModel.base64ToImageBitmap(pfp)
                                    }
                                    if (image != null) {
                                        Image(
                                            bitmap = image,
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = 3.dp,
                                                    color = MaterialTheme.colorScheme.primary, // Orange accent
                                                    shape = CircleShape
                                                )
                                        )
                                    } else {
                                        CircleInitials(name ?: "G1")
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Username Input
                                // Ensure EditableAccountField uses MaterialTheme colors internally!
                                EditableAccountField(
                                    label = "Name",
                                    value = name ?: "NO NAME",
                                    onValueChange = {
                                        name = it
                                        changed = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Friends and Flags stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatColumn(label = "Friends", count = friendsCount)
                                    Spacer(modifier = Modifier.width(32.dp))
                                    StatColumn(label = "Posts", count = postNr ?: 0)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Bio Input
                                EditableAccountField(
                                    label = "Bio",
                                    value = bio ?: "NO BIO",
                                    onValueChange = {
                                        bio = it
                                        changed = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Posts List Section
                                val currentPosts = postNames
                                if (currentPosts.isNullOrEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f) // Fill remaining space instead of fixed height
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "No posts yet!",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Time to explore the world.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f), // Fill remaining space
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        contentPadding = PaddingValues(bottom = 16.dp)
                                    ) {
                                        items(items = currentPosts) { flag ->
                                            FlagItem(flag, viewModel)
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }

                            // Save Button Logic
                            if (changed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val userUpdate = UserUpdate(
                                                    id = uD.id,
                                                    userName = name,
                                                    bio = bio,
                                                    userImage = pfp
                                                )
                                                viewModel.updateUser(uD.id, userUpdate)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary, // Warm Orange
                                            contentColor = MaterialTheme.colorScheme.onSecondary // White
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Save")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "SAVE CHANGES",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
