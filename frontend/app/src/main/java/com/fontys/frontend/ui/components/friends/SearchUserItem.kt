package com.fontys.frontend.ui.components.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fontys.frontend.ui.viewmodels.FriendsViewModel
import com.fontys.frontend.ui.viewmodels.RelationshipStatus

@Composable
fun SearchUserItem(
    user: com.fontys.frontend.data.models.User,
    relationshipStatus: RelationshipStatus,
    onSendRequest: () -> Unit,
    onViewProfile: () -> Unit,
    viewModel: FriendsViewModel
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
                RelationshipStatus.FRIENDS -> {
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
                RelationshipStatus.PENDING_SENT -> {
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
                RelationshipStatus.PENDING_RECEIVED -> {
                    Button(
                        onClick = {
                            val requestId = viewModel.getReceivedRequestIdForUser(user.id)
                            requestId?.let { viewModel.acceptFriendRequest(it) }
                        },
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
                RelationshipStatus.NONE -> {
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
