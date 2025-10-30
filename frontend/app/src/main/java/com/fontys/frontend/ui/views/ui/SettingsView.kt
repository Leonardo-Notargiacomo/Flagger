package com.fontys.frontend.ui.views.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column


@Composable
fun SettingsView(){
    MaterialTheme {
        // Surface provides a background color from the theme.
        Surface {
            Column {
            Text("User Settings")
            }
        }
    }
}

