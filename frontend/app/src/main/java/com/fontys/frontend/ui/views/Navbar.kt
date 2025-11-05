package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.fontys.frontend.common.FriendView
import com.fontys.frontend.common.MapView
import com.fontys.frontend.common.NavHost
import com.fontys.frontend.common.UserView

@Composable
fun NavBar() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()


    Scaffold(
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
                        contentDescription = "Map"
                    ) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {navController.navigate(UserView)},
                    icon = { Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Map"
                    ) }
                )

            }
        }) { padding ->
        NavHost(navController, padding)
    }
}