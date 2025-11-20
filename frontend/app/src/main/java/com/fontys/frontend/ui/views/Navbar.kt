package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.fontys.frontend.common.BadgeView
import com.fontys.frontend.common.FriendView
import com.fontys.frontend.common.MapView
import com.fontys.frontend.common.NavHost
import com.fontys.frontend.common.ProfileView

@Composable
fun NavBar() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = {navController.navigate(MapView)},
                    icon = { Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.Map,
                        contentDescription = "Map"
                    ) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {navController.navigate(FriendView)},
                    icon = { Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Friends"
                    ) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {navController.navigate(BadgeView)},
                    icon = { Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Badges"
                    ) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {navController.navigate(ProfileView)},
                    icon = { Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile"
                    ) }
                )

            }
        }) { padding ->
        NavHost(navController, padding)
    }
}