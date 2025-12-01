package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.UserUpdate
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.components.*
import com.fontys.frontend.ui.theme.ProfileColors
import com.fontys.frontend.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch




@Composable
fun ProfileScreen(userViewModel: ProfileViewModel = viewModel()) {
    val user by userViewModel.user.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Load user data once when screen opens
    LaunchedEffect(Unit) {
        userViewModel.getUser(UserRepository.userId.toString())
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
                    .border(3.dp, ProfileColors.Border, RoundedCornerShape(24.dp))
            ) {
                ProfileHeader()

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ProfileColors.Primary)
                        }
                    }
                    error != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = error ?: "Unknown error", color = ProfileColors.Primary)
                        }
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
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            ProfilePictureSection(
                                username = if (isEditing) editUsername else userData.userName,
                                isEditing = isEditing,
                                onImageEdit = { /* TODO */ }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

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
                                                    bio = editBio,
                                                    userImage = "Empty"
                                                )
                                                userViewModel.updateUser(userData.id, userUpdate)
                                                isEditing = false
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(2.dp, ProfileColors.Border, RoundedCornerShape(12.dp)),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ProfileColors.Accent,
                                            contentColor = ProfileColors.Primary
                                        )
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Save")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("SAVE CHANGES")
                                    }

                                    Button(
                                        onClick = { isEditing = false },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(2.dp, ProfileColors.Border, RoundedCornerShape(12.dp)),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ProfileColors.Container,
                                            contentColor = ProfileColors.Primary
                                        )
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("CANCEL")
                                    }
                                } else {
                                    Button(
                                        onClick = { isEditing = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(2.dp, ProfileColors.Border, RoundedCornerShape(12.dp)),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ProfileColors.Accent,
                                            contentColor = ProfileColors.Primary
                                        )
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = "Edit")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("EDIT PROFILE")
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
