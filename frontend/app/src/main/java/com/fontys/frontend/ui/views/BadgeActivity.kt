package com.fontys.frontend.ui.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fontys.frontend.ui.theme.BadgeTheme

class BadgeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Replace hardcoded userId with actual authenticated user's ID
        // Once authentication is merged, get userId from:
        // - SharedPreferences/DataStore
        // - Intent extra
        // - Authentication token/session
        val userId = 1

        setContent {
            BadgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BadgeScreen(userId = userId)
                }
            }
        }
    }
}
