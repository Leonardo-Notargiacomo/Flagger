package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.ui.viewmodels.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(viewModel: MapsViewModel = viewModel()) {
    val userLocation by viewModel.userLocation.collectAsState()
    val places by viewModel.places.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) { viewModel.loadUserLocation() }

    userLocation?.let {
        LaunchedEffect(it) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                viewModel.fetchNearbyPlaces(latLng)
            },
            properties = MapProperties(
                isMyLocationEnabled = true,
            )
        ) {
            places.forEach { place ->
                Marker(
                    state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                    title = place.displayName
                )
            }
        }

        // Zoom + Recenter Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Button(onClick = {
            }) { Text("＋") }

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
            }) { Text("－") }

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                userLocation?.let {
                }
            }) { Text("Center") }
        }
    }
}
