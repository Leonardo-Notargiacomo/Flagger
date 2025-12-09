package com.fontys.frontend.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fontys.frontend.data.UserUpdate
import com.fontys.frontend.data.models.FlagShowData
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.components.EditableAccountField
import com.fontys.frontend.ui.components.ProfileHeader
import com.fontys.frontend.ui.theme.ProfileColors
import com.fontys.frontend.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(viewModel: ProfileViewModel) {
    //Local variables for the profile
    var pfp: String? by remember { mutableStateOf("") }
    var name: String? by remember { mutableStateOf("") }
    var bio: String? by remember { mutableStateOf("") }

    // Dynamic data
    val friends by viewModel.friendsNr.collectAsState()
    val friendsCount = friends ?: 0
    val posts by viewModel.flag.collectAsState()
    val postNr by viewModel.flagNrs.collectAsState()
    val postNames by viewModel.flagName.collectAsState()

    //ViewModel variables
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var changed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getUser(UserRepository.userId.toString())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileColors.Container)
                    .border(3.dp, ProfileColors.Border, RoundedCornerShape(24.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileHeader()
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ProfileColors.Primary)
                        }
                    }

                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = error ?: "Unknown error", color = ProfileColors.Primary)
                        }
                    }

                    else -> {
                        LaunchedEffect(user) {
                            user?.let { uD ->
                                // Only update local state if we are essentially initializing
                                // (or force update on fresh load)
                                pfp = uD.userImage
                                name = uD.userName
                                bio = uD.bio
                            }
                        }
                        user?.let { uD ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TopAppBar(
                                    title = {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Account")
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.Transparent
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

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
                                                .border(2.dp, ProfileColors.Border, CircleShape)
                                        )
                                    } else {
                                        CircleInitials(name ?: "G1")
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                //Username
                                EditableAccountField(
                                    label = "name",
                                    value = name ?: "NO NAME",
                                    onValueChange = {
                                        name = it
                                        changed = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                //Friends and Flags list
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("Friends", style = MaterialTheme.typography.labelLarge)
                                        Text(friendsCount.toString(), style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Spacer(modifier = Modifier.width(32.dp))
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("Posts", style = MaterialTheme.typography.labelLarge)
                                        Text(postNr.toString(), style = MaterialTheme.typography.bodyLarge)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                EditableAccountField(
                                    label = "bio",
                                    value = bio ?: "NO BIO",
                                    onValueChange = {
                                        bio = it
                                        changed = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // LOGIC CHANGE IS HERE
                                val currentPosts = postNames
                                if (currentPosts.isNullOrEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .border(1.dp, ProfileColors.Border.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No posts yet! Time to explore",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        items(items = currentPosts) { flag ->
                                            FlagItem(flag, viewModel)
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }

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
                                                    userImage = ""
                                                )
                                                viewModel.updateUser(uD.id, userUpdate)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                2.dp,
                                                ProfileColors.Border,
                                                RoundedCornerShape(12.dp)
                                            ),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ProfileColors.Accent,
                                            contentColor = ProfileColors.Primary
                                        )
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Save")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("SAVE CHANGES")
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

@Composable
fun CircleInitials(name: String, size: Dp = 120.dp) {
    val initials = name
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(ProfileColors.Accent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.headlineMedium,
            color = ProfileColors.Primary
        )
    }
}

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    X: () -> Unit = {},
    Y: () -> Unit = {},
    Z: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("X") },
                onClick = {
                    expanded = false
                    X()
                }
            )
            DropdownMenuItem(
                text = { Text("Y") },
                onClick = {
                    expanded = false
                    Y()
                }
            )
            DropdownMenuItem(
                text = { Text("Z") },
                onClick = {
                    expanded = false
                    Z()
                }
            )
        }
    }
}

@Composable
fun FlagItem(flag: FlagShowData, viewModel: ProfileViewModel) {
    val image = viewModel.base64ToImageBitmap(flag.photoCode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = flag.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = flag.dateTaken, style = MaterialTheme.typography.bodySmall)
            }
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = "Image",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}