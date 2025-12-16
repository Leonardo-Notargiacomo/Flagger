package com.fontys.frontend.ui.views

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fontys.frontend.R
import com.fontys.frontend.common.CameraView
import com.fontys.frontend.data.PlaceService
import com.fontys.frontend.ui.components.BadgeUnlockDialog
import com.fontys.frontend.domain.UserRepository
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.fontys.frontend.ui.viewmodels.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLng
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import com.google.android.gms.maps.model.PinConfig
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor
import androidx.core.graphics.toColorInt
import androidx.emoji2.emojipicker.EmojiPickerView
import androidx.emoji2.emojipicker.RecentEmojiProvider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(navController: NavController, viewModel: MapsViewModel = viewModel()) {
    val isDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val userLocation by viewModel.userLocation.collectAsState()
    val fullFlags by viewModel.userFullFlags.collectAsState()
    val places by viewModel.places.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    var showDialog by remember{ mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<PlaceService?>(null) }
    val context = LocalContext.current
    val selectedPlaces = remember { mutableStateListOf<PlaceService>() }
    val userFlags by viewModel.userFlags.collectAsState()
    val currentUserId = UserRepository.userId
    val flagStyle by viewModel.flagStyle.collectAsState()
    var mapSettings by remember{ mutableStateOf(false) }
    var selectedMarkerId by remember { mutableStateOf<String?>("") }

    // Check if location permission is granted - continuously reactive
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Poll for permission changes every second while on this screen
    LaunchedEffect(Unit) {
        while (true) {
            val newPermissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (newPermissionState != hasLocationPermission) {
                hasLocationPermission = newPermissionState
                if (newPermissionState) {
                    // Permission just granted, reload location
                    viewModel.loadUserLocation()
                }
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    // Badge unlock dialog state
    val showBadgeDialog by viewModel.showBadgeDialog.collectAsState()
    val newlyUnlockedBadges by viewModel.newlyUnlockedBadges.collectAsState()
    LaunchedEffect(currentUserId) {
        if (currentUserId != null || currentUserId !=0) {
            viewModel.getFlags(currentUserId)
        }
    }

    // Check for pending badge unlocks when returning from camera
    LaunchedEffect(Unit) {
        viewModel.checkPendingBadges()
    }

    userLocation?.let {
        LaunchedEffect(it) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
        }
    }

    // Use Google Map ID for custom styling
//    val googleMapOptions = remember {
//        GoogleMapOptions().mapId("349a2b06249ce5213e12a47b")
//    }

    val mapProperties by remember(isDarkTheme, hasLocationPermission) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    if (isDarkTheme) R.raw.map_style_dark else R.raw.map_style
                )
            )
        )
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            val pinConfigBuilder: PinConfig.Builder = PinConfig.builder()


            userFlags.forEach { spot ->


                val pinConfig = PinConfig.builder()
                    .setGlyph(PinConfig.Glyph(if (flagStyle.emoji.isNullOrBlank()) "🏳️" else flagStyle.emoji))
                    .setBackgroundColor((if (flagStyle.background.isNullOrBlank()) "#E98D58" else flagStyle.background).toColorInt())
                    .setBorderColor((if (flagStyle.border.isNullOrBlank()) "#523735" else flagStyle.border).toColorInt())
                    .build()

                val markerState = remember {
                    MarkerState(
                        position = LatLng(
                            spot.location.latitude,
                            spot.location.longitude
                        )
                    )
                }

                AdvancedMarker(
                    state = markerState,
                    title = spot.displayName,
                    snippet = "",
                    pinConfig = pinConfig,
                    onClick = {
                        selectedMarkerId = spot.locationId
                        markerState.showInfoWindow()
                        true
                    }
                )
            }

        }

        // Flag button with explorer theme
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    viewModel.fetchNearbyPlaces(LatLng(userLocation.latitude,userLocation.longitude))
                    showDialog = true
                    selectedPlace = null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Flag Location",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 56.dp, start = 16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    mapSettings = true
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        // My Location button - styled for explorer theme
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    userLocation?.let {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(it, 15f)
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showDialog) {
        if (places.isEmpty()) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        "No Places Found",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                text = {
                    Text(
                        "Try again or move to another location.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            )
        } else {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        "Select a Place to Mark",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                text = {
                    Column {
                        val unflaggedPlaces = places.filter { nearbyPlace ->
                            !userFlags.any { flaggedSpot -> flaggedSpot.locationId == nearbyPlace.id }
                        }
                        if (unflaggedPlaces.isEmpty()) {
                            Text(
                                "All nearby places are already flagged!",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            unflaggedPlaces.forEach { place ->
                                TextButton(
                                    onClick = {
                                        showDialog = false
                                        scope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(place.latitude, place.longitude),
                                                    16f
                                                )
                                            )
                                        }
                                        picturedata.currentUserId = currentUserId
                                        picturedata.place_id = place.id
                                        navController.navigate(CameraView)
                                        scope.launch {
                                            delay(10000L)
                                        }
                                        viewModel.refreshFlags()
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Text(
                                        text = place.displayName,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                            }
                        }
                    }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDialog = false },
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            )
        }
    }

    // Badge unlock celebration dialog
    if (showBadgeDialog && newlyUnlockedBadges.isNotEmpty()) {
        BadgeUnlockDialog(
            badges = newlyUnlockedBadges,
            onDismiss = { viewModel.dismissBadgeDialog() }
        )
    }
    if(mapSettings){
        CustomFlagSettingsPopup(
            context,
            initialEmoji = flagStyle.emoji,
            initialBackground = flagStyle.background ?: "#E98D58", // Warm orange - app primary color
            initialBorder = flagStyle.border ?: "#523735", // Dark brown - app text color

            onDismiss = { mapSettings = false },

            onSave = { emoji, background, border ->
                viewModel.updateFlagStyle(emoji, background, border)
                mapSettings = false
            }
        )
    }
}


@Composable
fun ColorCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(48.dp)
            .shadow(
                elevation = if (selected) 4.dp else 0.dp,
                shape = CircleShape
            )
            .background(color, CircleShape)
            .border(
                width = if (selected) 3.dp else 2.dp,
                color = if (selected) Color(0xFFE98D58) else Color(0xFFC4B5A0),
                shape = CircleShape
            )
            .clickable { onClick() }
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = if (color.luminance() > 0.5) Color.Black else Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
            )
        }
    }
}
@Composable
fun CustomFlagSettingsPopup(
    context: Context,
    initialEmoji: String,
    initialBackground: String,
    initialBorder: String,
    onDismiss: () -> Unit,
    onSave: (emoji: String, background: String, border: String) -> Unit
) {
    var ibackground by remember {
        mutableStateOf(Color(initialBackground.toColorInt()))
    }

    var iborder by remember {
        mutableStateOf(Color(initialBorder.toColorInt()))
    }

    var emoji by remember { mutableStateOf(initialEmoji) }
    var background by remember { mutableStateOf(initialBackground) }
    var border by remember { mutableStateOf(initialBorder) }

    // Explorer-themed color palette with warm, vintage tones
    val presetColors = listOf(
        Color(0xFFE98D58), // Warm orange - primary
        Color(0xFF8B6F47), // Muted brown
        Color(0xFFD4956C), // Coral orange
        Color(0xFF5C4738), // Dark brown
        Color(0xFFB85042), // Terra cotta
        Color(0xFF6B9080), // Sage green
        Color(0xFF4A362A), // Deep brown
        Color(0xFFEBE3CD), // Parchment cream
        Color(0xFF2D2420), // Charcoal brown
        Color(0xFFC4B5A0), // Light tan
        Color(0xFF8B4513), // Saddle brown
        Color(0xFFD4C5B0)  // Warm beige
    )

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with decorative accent
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Customize Your Flag",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Decorative underline
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(3.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }

                // Prominent Flag Preview with shadow and decorative frame
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(8.dp, CircleShape)
                            .background(ibackground, CircleShape)
                            .border(6.dp, iborder, CircleShape)
                            .padding(4.dp)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 52.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Flag Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                // Emoji Picker Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Choose Your Symbol",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        )
                    ) {
                        AndroidView(
                            factory = {
                                EmojiPickerView(context).apply {
                                    setOnEmojiPickedListener { picked ->
                                        emoji = picked.emoji
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(8.dp)
                        )
                    }
                }

                // Background Color Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(ibackground, CircleShape)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        )

                        Text(
                            text = "Background Color",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetColors.forEach { color ->
                            ColorCircle(
                                color = color,
                                selected = ibackground == color,
                                onClick = { ibackground = color }
                            )
                        }
                    }
                }

                // Border Color Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color.Transparent, CircleShape)
                                .border(
                                    3.dp,
                                    iborder,
                                    CircleShape
                                )
                        )

                        Text(
                            text = "Border Color",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetColors.forEach { color ->
                            ColorCircle(
                                color = color,
                                selected = iborder == color,
                                onClick = { iborder = color }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            onSave(
                                emoji,
                                ibackground.toHexString(),
                                iborder.toHexString()
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Flag",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun Color.toHexString(): String {
    return String.format(
        "#%02X%02X%02X",
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}


