package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fontys.frontend.R
import com.fontys.frontend.common.CameraView
import com.fontys.frontend.data.PlaceService
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.domain.fromBase64
import com.fontys.frontend.ui.viewmodels.CameraPreviewViewModel
import com.fontys.frontend.ui.viewmodels.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.wait

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(navController: NavController, viewModel: MapsViewModel = viewModel()) {
    val userLocation by viewModel.userLocation.collectAsState()
    val fullFlags by viewModel.userFullFlags.collectAsState()
    val places by viewModel.places.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    var showDialog by remember{ mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<PlaceService?>(null) }
    val context = LocalContext.current
    val selectedPlaces = remember { mutableStateListOf<PlaceService>() }
    LaunchedEffect(Unit) { viewModel.loadUserLocation() }
    val userFlags by viewModel.userFlags.collectAsState()
    val currentUserId = UserRepository.userId

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
    Box(Modifier.fillMaxSize().padding(bottom = 130.dp)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
            ),
            googleMapOptionsFactory = { googleMapOptions }
        ) {

            userFlags.forEach { spot ->
                val pictureStr = fullFlags.find { marked -> marked.locationId.equals(spot.lcoationId)}

                Marker(
                    state = MarkerState(position = LatLng(spot.location.latitude, spot.location.longitude)),
                    title = spot.displayName,
                    snippet = "Flagged by you",
                    icon = fromBase64(pictureStr?.photoCode?:"")
                )
        }

        }

        // Zoom + Recenter Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(6.dp),
            horizontalAlignment = Alignment.End
        )

        {

            Button(onClick = {
                viewModel.fetchNearbyPlaces(LatLng(userLocation.latitude,userLocation.longitude))
                if(places.size==1){
                    picturedata.currentUserId = currentUserId
                    picturedata.place_id = places.get(0).id
                    navController.navigate(CameraView)
                } else {
                    showDialog = true
                    selectedPlace = null
                }
            }) { Text("\uD83D\uDEA9") }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        if (places.isEmpty()) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("No places found") },
                text = { Text("Try again or move to another location.") },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Select a place to mark") },
                text = {
                    Column {
                        val unflaggedPlaces = places.filter { nearbyPlace ->
                            !userFlags.any { flaggedSpot -> flaggedSpot.lcoationId == nearbyPlace.id }
                        }
                        if (unflaggedPlaces.isEmpty()) {
                        Text("All nearby places are already flagged!")
                        }
                        else {
                        unflaggedPlaces.forEach { place ->
                            TextButton(onClick = {

                                showDialog = false
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(place.latitude, place.longitude),
                                            16f
                                        )
                                        )
                                    picturedata.currentUserId = currentUserId
                                    picturedata.place_id = place.id
                                    navController.navigate(CameraView)

                                }

                            }) {
                                Text(place.displayName)
                            }
                        }
                    }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

