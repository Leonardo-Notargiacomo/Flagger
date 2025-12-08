package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fontys.frontend.domain.UserRepository

// Dummy composables for tab content
@Composable fun RecentPostsScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Recent Posts") } }
@Composable fun RecentUsersScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Recent Users") } }
@Composable fun RecentOffendersScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Recent Offenders") } }

@Composable
fun AdminScreen(mainNavController: NavController) {
    val adminNavController = rememberNavController()
    Scaffold(
        bottomBar = {
            AdminNavBar(
                navController = adminNavController,
                onExit = {
                    // Clear user data and go back to login
                    UserRepository.token = ""
                    UserRepository.userId = 0
                    mainNavController.navigate("login") {
                        popUpTo(mainNavController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = adminNavController,
            startDestination = "recent_posts",
            modifier = Modifier.padding(padding)
        ) {
            composable("recent_posts") { RecentPostsScreen() }
            composable("recent_users") { RecentUsersScreen() }
            composable("recent_offenders") { RecentOffendersScreen() }
        }
    }
}

@Composable
fun AdminNavBar(navController: NavController, onExit: () -> Unit) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val items = listOf(
        AdminNavItem("Posts", Icons.AutoMirrored.Filled.Article, "recent_posts"),
        AdminNavItem("Users", Icons.Default.People, "recent_users"),
        AdminNavItem("Offenders", Icons.Default.Gavel, "recent_offenders"),
        AdminNavItem("Exit", Icons.AutoMirrored.Filled.ExitToApp, "exit")
    )

    NavigationBar {

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selectedIndex == index,
                onClick = {
                    if (item.route == "exit") {
                        onExit()
                    } else {
                        selectedIndex = index
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

data class AdminNavItem(val title: String, val icon: ImageVector, val route: String)
