package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeleteAccountScreen(
    onConfirmDelete: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.error.copy(alpha = 0.08f))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Danger",
            tint = colors.error,
            modifier = Modifier.height(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Delete Account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This action is permanent. All your data, posts, and connections will be removed. Are you absolutely sure you want to continue?",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onBackground,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onConfirmDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.error,
                contentColor = colors.onError
            )
        ) {
            Text(
                "Yes, delete my account",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.error
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.error)
        ) {
            Text(
                "Cancel",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
