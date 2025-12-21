package com.fontys.frontend.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fontys.frontend.data.FlagResponse
import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.AdminViewModel

@Composable
fun RecentPostsScreen(viewModel: AdminViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    val flags = uiState.flags

    LaunchedEffect(Unit) {
        viewModel.loadFlags()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(flags.entries.toList()) { entry ->
            FlagCard(flag = entry.key, displayInfo = entry.value, viewModel = viewModel)
        }
    }

}

@Composable
fun FlagCard(
    flag: FlagResponse,
    displayInfo: com.fontys.frontend.ui.viewmodels.FlagDisplayInfo,
    viewModel: AdminViewModel = viewModel()
) {
    Card(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "User: ${displayInfo.userName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = "Location: ${displayInfo.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            val image = viewModel.base64ToImageBitmap(flag.photoCode)
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = "Flag Photo",
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                )
            } else {
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "No Image Available",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

}

@Composable
fun RecentUsersScreen(viewModel: AdminViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val users = uiState.users

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(users) { user ->
            UserCard(user)
        }
    }


}

@Composable
fun UserCard (user: UserReturn, viewModel: AdminViewModel = viewModel()) {
    Card(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "User: ${user.userName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = "Bio: ${user.bio}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            val image = viewModel.base64ToImageBitmap(user.userImage)
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                )
            } else {
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "No Image Available",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

    }

}
@Composable
fun RecentOffendersScreen(viewModel: AdminViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val users = uiState.users

    LaunchedEffect(Unit) {
        viewModel.filterUsers()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(users) { user ->
            OffenderCard(user, viewModel)
        }
    }
}

@Composable
fun OffenderCard(user: UserReturn, viewModel: AdminViewModel = viewModel()) {
    Card(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "User: ${user.userName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            // Emphasized bio section with warning styling
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⚠️ FLAGGED BIO:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            val image = viewModel.base64ToImageBitmap(user.userImage)
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                )
            } else {
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "No Image Available",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun AdminScreen(mainNavController: NavController, adminViewModel: AdminViewModel = viewModel()) {
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
            composable("recent_posts") { RecentPostsScreen(adminViewModel) }
            composable("recent_users") { RecentUsersScreen(adminViewModel) }
            composable("recent_offenders") { RecentOffendersScreen(adminViewModel) }
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

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
        )

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
                },
                colors = navItemColors
            )
        }
    }
}

data class AdminNavItem(val title: String, val icon: ImageVector, val route: String)
