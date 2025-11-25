package com.fontys.frontend.ui.views

import android.Manifest
import android.content.pm.PackageManager
import android.nfc.Tag
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fontys.frontend.R
import com.fontys.frontend.data.PlaceService
import com.fontys.frontend.ui.components.BadgeUnlockDialog
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(navController: NavController, viewModel: MapsViewModel = viewModel()) {
    val userLocation by viewModel.userLocation.collectAsState()
    val places by viewModel.places.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    var showDialog by remember{ mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<PlaceService?>(null) }
    val context = LocalContext.current
    val selectedPlaces = remember { mutableStateListOf<PlaceService>() }
    LaunchedEffect(Unit) { viewModel.loadUserLocation() }
    val userFlags by viewModel.userFlags.collectAsState()
    val currentUserId = UserRepository.userId

    // Check if location permission is granted
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Badge unlock dialog state
    val showBadgeDialog by viewModel.showBadgeDialog.collectAsState()
    val newlyUnlockedBadges by viewModel.newlyUnlockedBadges.collectAsState()
    LaunchedEffect(currentUserId) {
        if (currentUserId != null || currentUserId !=0) {
            viewModel.getFlags(currentUserId)
        }
    }

    userLocation?.let {
        LaunchedEffect(it) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
        }
    }
    val googleMapOptions = remember {
        GoogleMapOptions().mapId("349a2b06249ce52186cf3c94")
    }
    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
            ),
            googleMapOptionsFactory = { googleMapOptions }
        ) {

            userFlags.forEach { spot ->
                Marker(
                    state = MarkerState(position = LatLng(spot.location.latitude, spot.location.longitude)),
                    title = spot.displayName,
                    snippet = "Flagged by you",
                    alpha = 0.7f
                )
        }
            places.forEach { place ->
                val isAlreadyFlagged = userFlags.any { flaggedSpot -> flaggedSpot.locationId ==place.id }
                if (!isAlreadyFlagged) {
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        title = place.displayName,
                        snippet = "Nearby"
                    )
                }
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
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(72.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "\uD83D\uDEA9",
                    fontSize = 32.sp
                )
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        if (places.isEmpty()) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        "No Places Found",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
                        Text("OK", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
                                    viewModel.markTheSpot(
                                        userId = currentUserId,
                                        placeId = place.id,
                                        locationName = place.displayName,
                                        latLng = LatLng(place.latitude, place.longitude)
                                    )
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(place.latitude, place.longitude),
                                                16f
                                            )
                                        )
                                    }
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
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
}

