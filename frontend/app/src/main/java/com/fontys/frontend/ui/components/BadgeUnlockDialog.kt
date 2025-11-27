package com.fontys.frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.fontys.frontend.data.models.Badge

/**
 * Dialog to celebrate newly unlocked badges
 * Displays when user unlocks one or more badges by exploring locations
 */
@Composable
fun BadgeUnlockDialog(
    badges: List<Badge>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Celebration header
                Text(
                    text = if (badges.size == 1) "🎊 NEW BADGE! 🎊" else "🎊 NEW BADGES! 🎊",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Congratulations!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Badge list (scrollable if multiple)
                if (badges.size == 1) {
                    // Single badge - larger display
                    BadgeItem(badge = badges[0], isLarge = true)
                } else {
                    // Multiple badges - scrollable list
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(badges) { badge ->
                            BadgeItem(badge = badge, isLarge = false)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Continue button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Continue Exploring",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Individual badge item display
 */
@Composable
private fun BadgeItem(badge: Badge, isLarge: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Badge icon
        Box(
            modifier = Modifier
                .size(if (isLarge) 120.dp else 80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            // Display emoji or icon
            Text(
                text = badge.iconUrl ?: "🏆",
                fontSize = if (isLarge) 64.sp else 48.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Badge name
        Text(
            text = badge.name,
            fontSize = if (isLarge) 20.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Badge description
        Text(
            text = badge.description,
            fontSize = if (isLarge) 16.sp else 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
