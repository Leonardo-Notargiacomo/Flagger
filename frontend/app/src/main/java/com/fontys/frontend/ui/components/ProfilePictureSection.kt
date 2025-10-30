package com.fontys.frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fontys.frontend.ui.theme.ProfileColors

/**
 * Profile picture section with decorative frame and optional edit button
 * Features: Framed profile image, username display, decorative elements
 */
@Composable
fun ProfilePictureSection(
    username: String,
    profileImageUrl: String? = null,
    isEditing: Boolean = false,
    onImageEdit: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Decorative corner dots (top)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top left dots
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ProfileColors.Accent)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ProfileColors.Primary)
                    )
                }

                // Top right dots
                Row(
                    modifier = Modifier.padding(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ProfileColors.Primary)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ProfileColors.Accent)
                    )
                }
            }

            // Profile picture with frames
            Box(contentAlignment = Alignment.BottomEnd) {
                // Outer frame (dark brown)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ProfileColors.Primary)
                        .padding(8.dp)
                ) {
                    // Inner frame (orange)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ProfileColors.Accent)
                            .padding(4.dp)
                    ) {
                        // Profile image placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ProfileColors.Container),
                            contentAlignment = Alignment.Center
                        ) {
                            // Account icon placeholder
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile Picture",
                                tint = ProfileColors.Primary.copy(alpha = 0.3f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                // Edit camera button (shown when editing)
                if (isEditing) {
                    IconButton(
                        onClick = onImageEdit,
                        modifier = Modifier
                            .offset(x = 8.dp, y = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ProfileColors.Accent)
                            .border(2.dp, ProfileColors.Primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Picture",
                            tint = ProfileColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Username badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileColors.Primary)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = username.uppercase().ifEmpty { "EXPLORER" },
                    color = ProfileColors.TextSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 2.sp
                )
            }

            // Decorative divider line with center dot
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.75f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(ProfileColors.Accent)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ProfileColors.Primary)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(ProfileColors.Accent)
                )
            }
        }
    }
}
