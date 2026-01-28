package com.fontys.frontend.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fontys.frontend.R
import kotlin.random.Random

/**
 * Nihilist Penguin Quote Dialog
 *
 * Displays a dialog featuring the iconic documentary scene with
 * an inspirational existential quote about exploration.
 *
 * Part of the "Nihilist Penguin" Easter egg - a reference to the
 * viral meme from Werner Herzog's documentary.
 */

private val nihilistQuotes = listOf(
    "Every flag you plant marks a moment you chose adventure over comfort.",
    "The best discoveries happen when the map runs out.",
    "In the vastness of the world, your footsteps still matter.",
    "Every new place leaves its mark on you. Go collect them.",
    "You don't need a destination. You need a direction.",
    "The horizon is just the start of somewhere new.",
    "Not all who wander are lost. Some are just collecting flags.",
    "Adventure doesn't wait for perfect weather. Neither should you.",
    "Beyond the familiar lies everything worth discovering.",
    "That penguin walked toward the mountain because it was there. What's your mountain?",
    "Real explorers don't check the weather. They check the map.",
    "A flag is just proof you were brave enough to go.",
    "The world is full of places waiting for your footprints."
)

@Composable
fun PenguinQuoteDialog(
    lastQuoteIndex: Int? = null,
    onDismiss: (Int) -> Unit
) {
    // Select a random quote, ensuring it's different from the last one
    val (quote, currentIndex) = remember(lastQuoteIndex) {
        val availableIndices = nihilistQuotes.indices.filter { it != lastQuoteIndex }
        val newIndex = availableIndices.random()
        nihilistQuotes[newIndex] to newIndex
    }

    Dialog(onDismissRequest = { onDismiss(currentIndex) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image from the documentary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nihilist_penguin_bg),
                        contentDescription = "Penguin walking toward mountains",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Subtle dark overlay for mood
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.15f))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quote text
                Text(
                    text = "\"$quote\"",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Dismiss button
                Button(
                    onClick = { onDismiss(currentIndex) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Keep Exploring",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
