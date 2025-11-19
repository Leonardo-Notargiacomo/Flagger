package com.fontys.frontend.ui.viewmodels


import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.FlagDisplay
import com.fontys.frontend.data.PlaceService
import com.fontys.frontend.domain.FlagRepository
import com.fontys.frontend.domain.MapRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.checkerframework.checker.units.qual.m
import org.json.JSONObject
import java.io.IOException

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val mapRepository = MapRepository()
    private val flagRepository = FlagRepository()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val client = OkHttpClient()

    private val _userFlags = MutableStateFlow<List<FlagDisplay>>(emptyList())

    val userFlags: StateFlow<List<FlagDisplay>> = _userFlags

    private val _userLocation = MutableStateFlow(LatLng(0.0, 0.0))
    val userLocation: StateFlow<LatLng> = _userLocation

    private val _places = MutableStateFlow<List<PlaceService>>(emptyList())
    val places: StateFlow<List<PlaceService>> = _places

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun loadUserLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _userLocation.value = LatLng(it.latitude, it.longitude)
                }
            }
        } else {
            _error.value = "Location permission not granted."
        }
    }
    fun markTheSpot(userId: Int, placeId: String, photoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = flagRepository.addFlag(userId,placeId,photoId)
                getFlags(userId)
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "The place has not been marked"
                Log.e("MapsViewModel", "Error marking the place", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun getFlags(userId: Int){
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = flagRepository.getFlags(userId);

                val spots = mapRepository.getLatlngs(result)
                spots.onSuccess { details ->  _userFlags.value = details }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "The flags are not gathered"
                Log.e("MapsViewModel", "Error finding the places", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchNearbyPlaces(latlng: LatLng) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = mapRepository.markTheSpot(latlng)
                _places.value = result
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error fetching places"
                _places.value = emptyList()
                Log.e("MapsViewModel", "Error fetching nearby places", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

