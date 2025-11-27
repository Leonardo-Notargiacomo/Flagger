package com.fontys.frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Read-only account field component with map-style label tag
 * Features: Label badge, field container with border, decorative corner dots
 */
@Composable
fun AccountField(
    label: String,
    value: String,
    multiline: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Field container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(
                    top = 24.dp,
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            // Field value
            Text(
                text = value.ifEmpty { "—" },
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (multiline) Modifier.heightIn(min = 60.dp)
                        else Modifier
                    )
            )
        }

        // Label badge (positioned above the field)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 12.dp, y = (-10).dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp
            )
        }
    }
}
