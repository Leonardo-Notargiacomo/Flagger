package com.fontys.frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fontys.frontend.ui.theme.ProfileColors

/**
 * Editable account field component for edit mode
 * Features: Label badge, text input field with border
 */
@Composable
fun EditableAccountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
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
                .background(ProfileColors.Container)
                .border(2.dp, ProfileColors.Border, RoundedCornerShape(12.dp))
                .padding(
                    top = 24.dp,
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            // Text input field
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = ProfileColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(ProfileColors.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (multiline) Modifier.heightIn(min = 60.dp)
                        else Modifier
                    ),
                maxLines = if (multiline) Int.MAX_VALUE else 1,
                singleLine = !multiline
            )
        }

        // Label badge (positioned above the field)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 12.dp, y = (-10).dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ProfileColors.Primary)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = ProfileColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp
            )
        }
    }
}
