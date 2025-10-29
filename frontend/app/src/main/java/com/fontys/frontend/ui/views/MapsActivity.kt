package com.fontys.frontend.ui.views

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity

class MapsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapsScreen()
        }
    }
}