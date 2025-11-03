
package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Badge
import com.fontys.frontend.ui.viewmodels.BadgeViewModel
import com.fontys.frontend.ui.viewmodels.BadgeUiState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeScreen(
    userId: Int,
    viewModel: BadgeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadUserBadges(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Badges") }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is BadgeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is BadgeUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Stats card
                    BadgeStatsCard(
                        earnedBadges = state.earnedBadges,
                        totalBadges = state.totalBadges
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Badge grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.badges) { badge ->
                            BadgeItem(
                                badge = badge,
                                onClick = { /* Show detail dialog */ }
                            )
                        }
                    }
                }
            }

            is BadgeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshBadges(userId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeStatsCard(earnedBadges: Int, totalBadges: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$earnedBadges",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Earned", fontSize = 14.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalBadges",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Total", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun BadgeItem(badge: Badge, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = badge.iconUrl ?: "🏆",
                fontSize = 32.sp,
                modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.3f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = badge.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.5f)
            )
        }

}}
