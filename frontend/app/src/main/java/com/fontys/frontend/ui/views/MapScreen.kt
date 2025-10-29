package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fontys.frontend.data.PlaceService
import com.fontys.frontend.ui.viewmodels.MapsViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@Composable
fun MapsScreen(viewModel: MapsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val userLocation by viewModel.userLocation.collectAsState()
    val places by viewModel.places.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedMarkerList by remember { mutableStateOf(emptyList<PlaceService>()) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserLocation()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            onMapClick = { latLng ->
                viewModel.fetchNearbyPlaces(latLng)
                showDialog = true
            }
        ) {
            // User marker
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "You are here"
                )
            }

            // Selected markers
            selectedMarkerList.forEach { place ->
                Marker(
                    state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                    title = place.displayName
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (!error.isNullOrEmpty()) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            ) {
                Text(error!!)
            }
        }

        if (showDialog && places.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Select a place") },
                text = {
                    Column {
                        places.forEach { place ->
                            TextButton(onClick = {
                                selectedMarkerList = selectedMarkerList + place
                                showDialog = false
                            }) {
                                Text(place.displayName)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

