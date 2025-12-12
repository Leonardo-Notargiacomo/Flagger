package com.fontys.frontend.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.*
import com.fontys.frontend.R
import com.fontys.frontend.data.models.Badge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dialog to celebrate newly unlocked badges
 * Displays when user unlocks one or more badges by exploring locations
 */
@Composable
fun BadgeUnlockDialog(
    badges: List<Badge>,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var canDismiss by remember { mutableStateOf(false) }
    
    // Confetti Animation State
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LaunchedEffect(Unit) {
        // Haptic Feedback
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        
        // Sound Effect
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(ToneGenerator.TONE_SUP_RINGTONE, 200)
            delay(200)
            toneGen.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Prevent immediate dismissal
        delay(2000)
        canDismiss = true
    }

    Dialog(onDismissRequest = { if (canDismiss) onDismiss() }) {
        Box(contentAlignment = Alignment.Center) {
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
                        enabled = canDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (canDismiss) "Continue Exploring" else "Celebration in progress...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Lottie Confetti Overlay
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.5f) // Make it bigger to cover more area
            )
        }
    }
}

/**
 * Individual badge item display
 */
@Composable
private fun BadgeItem(badge: Badge, isLarge: Boolean) {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        delay(100) // Slight delay for pop effect
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
    ) {
        // Badge icon
        Box(
            modifier = Modifier
                .size(if (isLarge) 120.dp else 80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            // Display vector icon
            Icon(
                imageVector = BadgeIcons.getIcon(badge.name, badge.id),
                contentDescription = badge.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (isLarge) 64.dp else 48.dp)
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
