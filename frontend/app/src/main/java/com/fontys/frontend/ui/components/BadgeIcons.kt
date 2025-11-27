package com.fontys.frontend.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps badge names or categories to specific Material Icons.
 * This replaces the use of emoji strings with consistent vector icons.
 */
object BadgeIcons {
    fun getIcon(name: String): ImageVector {
        return when {
            // First flag / Beginner
            name.contains("First", ignoreCase = true) -> Icons.Default.Flag
            name.contains("Beginner", ignoreCase = true) -> Icons.Default.Start
            
            // Exploration / Map
            name.contains("Explorer", ignoreCase = true) -> Icons.Default.Explore
            name.contains("Map", ignoreCase = true) -> Icons.Default.Map
            name.contains("World", ignoreCase = true) -> Icons.Default.Public
            name.contains("Travel", ignoreCase = true) -> Icons.Default.FlightTakeoff
            
            // Social / Friends
            name.contains("Friend", ignoreCase = true) -> Icons.Default.People
            name.contains("Social", ignoreCase = true) -> Icons.Default.Groups
            name.contains("Community", ignoreCase = true) -> Icons.Default.Diversity3
            
            // Streaks / Activity
            name.contains("Streak", ignoreCase = true) -> Icons.Default.LocalFireDepartment
            name.contains("Daily", ignoreCase = true) -> Icons.Default.Today
            name.contains("Active", ignoreCase = true) -> Icons.Default.DirectionsRun
            
            // Achievements / Mastery
            name.contains("Master", ignoreCase = true) -> Icons.Default.MilitaryTech
            name.contains("Legend", ignoreCase = true) -> Icons.Default.WorkspacePremium
            name.contains("Star", ignoreCase = true) -> Icons.Default.Star
            name.contains("Elite", ignoreCase = true) -> Icons.Default.Diamond
            
            // Specific counts
            name.contains("10", ignoreCase = true) -> Icons.Default.LooksOne
            name.contains("50", ignoreCase = true) -> Icons.Default.Looks5
            name.contains("100", ignoreCase = true) -> Icons.Default.Filter9Plus

            // Default fallback
            else -> Icons.Default.EmojiEvents
        }
    }
}
