package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fontys.frontend.domain.model.UserProfile
import com.fontys.frontend.ui.components.*
import com.fontys.frontend.ui.theme.ProfileColors

/**
 * Profile/Account screen with map-themed design
 * Features: View mode and edit mode for user profile information
 */
@Composable
fun ProfileScreen() {
    // State management
    var isEditing by remember { mutableStateOf(false) }
    var userData by remember {
        mutableStateOf(
            UserProfile(
                username = "MapExplorer",
                email = "explorer@mapquest.com",
                bio = "Passionate about discovering new territories and charting the unknown. Every journey tells a story."
            )
        )
    }
    var editData by remember { mutableStateOf(userData) }

    // Event handlers
    val handleEdit = {
        editData = userData.copy()
        isEditing = true
    }

    val handleSave = {
        userData = editData.copy()
        isEditing = false
    }

    val handleCancel = {
        editData = userData.copy()
        isEditing = false
    }

    val handleImageEdit = {
        // TODO: Implement image upload
    }

    // Main UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileColors.Background),
        contentAlignment = Alignment.Center
    ) {
        // Mobile container - scaled down
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            // Map-style container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileColors.Container)
                    .border(3.dp, ProfileColors.Border, RoundedCornerShape(24.dp))
            ) {
                // Header
                ProfileHeader()

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile picture and username
                    ProfilePictureSection(
                        username = if (isEditing) editData.username else userData.username,
                        profileImageUrl = userData.profileImageUrl,
                        isEditing = isEditing,
                        onImageEdit = handleImageEdit
                    )

                    // Account fields
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditing) {
                        // Edit mode - show editable fields
                        EditableAccountField(
                            label = "Username",
                            value = editData.username,
                            onValueChange = { editData = editData.copy(username = it) }
                        )

                        EditableAccountField(
                            label = "Email",
                            value = editData.email,
                            onValueChange = { editData = editData.copy(email = it) }
                        )

                        EditableAccountField(
                            label = "Bio",
                            value = editData.bio,
                            onValueChange = { editData = editData.copy(bio = it) },
                            multiline = true
                        )
                    } else {
                        // View mode - show read-only fields
                        AccountField(
                            label = "Username",
                            value = userData.username
                        )

                        AccountField(
                            label = "Email",
                            value = userData.email
                        )

                        AccountField(
                            label = "Bio",
                            value = userData.bio,
                            multiline = true
                        )
                    }

                    // Action buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isEditing) {
                            // Save button
                            Button(
                                onClick = handleSave,
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
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SAVE CHANGES",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 1.5.sp
                                )
                            }

                            // Cancel button
                            Button(
                                onClick = handleCancel,
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
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CANCEL",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        } else {
                            // Edit button
                            Button(
                                onClick = handleEdit,
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
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EDIT PROFILE",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
