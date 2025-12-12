package com.fontys.frontend.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.shimmerEffect(
    shimmerColor: Color = Color.White.copy(alpha = 0.5f),
    durationMillis: Int = 3000
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent,
            shimmerColor,
            Color.Transparent,
            Color.Transparent
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnimation, y = translateAnimation)
    )

    this.background(brush)
}
