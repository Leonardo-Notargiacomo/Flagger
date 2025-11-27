package com.fontys.frontend.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.sharp.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.abs

/**
 * Maps badge names or IDs to specific Material Icons.
 * Uses a combination of name matching and ID-based deterministic mapping
 * to ensure every badge has a unique-looking and consistent icon.
 */
object BadgeIcons {
    
    // A large pool of distinct icons for fallback assignment
    private val iconPool = listOf(
        Icons.Default.Star, Icons.Default.Favorite, Icons.Default.Bolt,
        Icons.Default.Diamond, Icons.Default.WorkspacePremium, Icons.Default.MilitaryTech,
        Icons.Default.EmojiEvents, Icons.Default.Handshake, Icons.Default.Lightbulb,
        Icons.Default.RocketLaunch, Icons.Default.Public, Icons.Default.Terrain,
        Icons.Default.Hiking, Icons.Default.Landscape, Icons.Default.Forest,
        Icons.Default.WaterDrop, Icons.Default.Park, Icons.Default.Deck,
        Icons.Default.Castle, Icons.Default.Church, Icons.Default.Stadium,
        Icons.Default.Business, Icons.Default.Museum, Icons.Default.Restaurant,
        Icons.Default.LocalCafe, Icons.Default.LocalBar, Icons.Default.Celebration,
        Icons.Default.CameraAlt, Icons.Default.Palette, Icons.Default.Brush,
        Icons.Default.MusicNote, Icons.Default.Headphones, Icons.Default.Book,
        Icons.Default.School, Icons.Default.Science, Icons.Default.Biotech,
        Icons.Default.Computer, Icons.Default.Smartphone, Icons.Default.Watch,
        Icons.Default.DirectionsCar, Icons.Default.DirectionsBike, Icons.Default.DirectionsBoat,
        Icons.Default.Flight, Icons.Default.Train, Icons.Default.LocalShipping,
        Icons.Default.Anchor, Icons.Default.Flag, Icons.Default.Map,
        Icons.Default.Explore, Icons.Default.CompassCalibration, Icons.Default.NearMe,
        Icons.Default.PinDrop, Icons.Default.Navigation, Icons.Default.Satellite,
        Icons.Default.Extension, Icons.Default.Casino, Icons.Default.SportsEsports,
        Icons.Default.SportsSoccer, Icons.Default.SportsBasketball, Icons.Default.SportsTennis,
        Icons.Default.FitnessCenter, Icons.Default.Pool, Icons.Default.Kayaking,
        Icons.Default.Surfing, Icons.Default.DownhillSkiing, Icons.Default.Snowboarding,
        Icons.Default.Whatshot, Icons.Default.AcUnit, Icons.Default.Tsunami,
        Icons.Default.Volcano, Icons.Default.Coronavirus, Icons.Default.Masks,
        Icons.Default.Recycling, Icons.Default.Nature, Icons.Default.NaturePeople,
        Icons.Default.Pets, Icons.Default.CrueltyFree, Icons.Default.BugReport,
        Icons.Default.PestControl, Icons.Default.Face, Icons.Default.Face3,
        Icons.Default.Face6, Icons.Default.Support, Icons.Default.SupportAgent,
        Icons.Default.Engineering, Icons.Default.Construction, Icons.Default.Architecture,
        Icons.Default.Apartment, Icons.Default.House, Icons.Default.Cottage,
        Icons.Default.Gavel, Icons.Default.Balance, Icons.Default.Savings,
        Icons.Default.Paid, Icons.Default.CurrencyBitcoin, Icons.Default.ShoppingBag,
        Icons.Default.ShoppingCart, Icons.Default.Sell, Icons.Default.LocalOffer,
        Icons.Default.Loyalty, Icons.Default.Verified, Icons.Default.NewReleases
    )

    fun getIcon(name: String, id: Int): ImageVector {
        // 1. Try specific name matching first for semantic accuracy
        return when {
            // Rarity / Rank
            name.contains("Legend", ignoreCase = true) -> Icons.Default.WorkspacePremium
            name.contains("Master", ignoreCase = true) -> Icons.Default.MilitaryTech
            name.contains("Elite", ignoreCase = true) -> Icons.Default.Diamond
            name.contains("Expert", ignoreCase = true) -> Icons.Default.Stars
            name.contains("Beginner", ignoreCase = true) -> Icons.Default.Start
            
            // Activity Types
            name.contains("Run", ignoreCase = true) -> Icons.Default.DirectionsRun
            name.contains("Walk", ignoreCase = true) -> Icons.Default.DirectionsWalk
            name.contains("Cycle", ignoreCase = true) || name.contains("Bike", ignoreCase = true) -> Icons.Default.DirectionsBike
            name.contains("Hike", ignoreCase = true) -> Icons.Default.Hiking
            
            // Specific Milestones (Using numbers in name)
            name.contains("100", ignoreCase = true) -> Icons.Default.Filter9Plus
            name.contains("50", ignoreCase = true) -> Icons.Default.Looks5
            name.contains("10", ignoreCase = true) -> Icons.Default.LooksOne
            name.contains("First", ignoreCase = true) -> Icons.Default.Flag

            // Exploration Categories
            name.contains("World", ignoreCase = true) -> Icons.Default.Public
            name.contains("City", ignoreCase = true) -> Icons.Default.LocationCity
            name.contains("Nature", ignoreCase = true) -> Icons.Default.NaturePeople
            name.contains("Water", ignoreCase = true) || name.contains("Ocean", ignoreCase = true) -> Icons.Default.WaterDrop
            name.contains("Mountain", ignoreCase = true) -> Icons.Default.Terrain
            
            // Social
            name.contains("Friend", ignoreCase = true) -> Icons.Default.PeopleAlt
            name.contains("Social", ignoreCase = true) -> Icons.Default.Diversity3
            name.contains("Community", ignoreCase = true) -> Icons.Default.Diversity1
            
            // Time/Streak
            name.contains("Streak", ignoreCase = true) -> Icons.Default.LocalFireDepartment
            name.contains("Daily", ignoreCase = true) -> Icons.Default.CalendarToday
            name.contains("Early", ignoreCase = true) -> Icons.Default.WbTwilight
            name.contains("Night", ignoreCase = true) -> Icons.Default.NightsStay

            // 2. If no keyword match, use deterministic hashing based on ID to pick a unique icon from the pool
            // This ensures that "Badge #45" always gets the same unique icon, different from "Badge #46"
            else -> {
                val index = abs(id.hashCode()) % iconPool.size
                iconPool[index]
            }
        }
    }
}