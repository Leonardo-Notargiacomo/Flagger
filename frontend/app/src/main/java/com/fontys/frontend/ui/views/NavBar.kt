package com.fontys.frontend.ui.views

import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.fontys.frontend.common.NavHost

@Composable
fun Navbar() {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

        NavigationBar(
            fo
    ) {
        NavHost()
    }
}