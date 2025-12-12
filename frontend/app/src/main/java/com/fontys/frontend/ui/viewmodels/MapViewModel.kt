package com.fontys.frontend.ui.viewmodels


import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.FlagDisplay
import com.fontys.frontend.data.FlagResponse
import com.fontys.frontend.data.PlaceService
import com.fontys.frontend.data.models.Badge
import com.fontys.frontend.data.models.ExplorationEvent
import com.fontys.frontend.data.models.StreakInfo
import com.fontys.frontend.data.repositories.BadgeRepository
import com.fontys.frontend.domain.FlagRepository
import com.fontys.frontend.domain.MapRepository
import com.fontys.frontend.ui.viewmodels.PendingBadgeUnlocks
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
    private val badgeRepository = BadgeRepository()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val client = OkHttpClient()

    private val _userFlags = MutableStateFlow<List<FlagDisplay>>(emptyList())

    val userFlags: StateFlow<List<FlagDisplay>> = _userFlags
    private val _userFullFlags = MutableStateFlow<List<FlagResponse>>(emptyList())

    val userFullFlags: StateFlow<List<FlagResponse>> = _userFullFlags

    private val _userLocation = MutableStateFlow(LatLng(0.0, 0.0))
    val userLocation: StateFlow<LatLng> = _userLocation

    private val _places = MutableStateFlow<List<PlaceService>>(emptyList())
    val places: StateFlow<List<PlaceService>> = _places

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _newlyUnlockedBadges = MutableStateFlow<List<Badge>>(emptyList())
    val newlyUnlockedBadges: StateFlow<List<Badge>> = _newlyUnlockedBadges

    private val _showBadgeDialog = MutableStateFlow(false)
    val showBadgeDialog: StateFlow<Boolean> = _showBadgeDialog


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
                saveFlag(userId, placeId, photoId)
                checkForBadgeUnlocks(userId, placeId)
                refreshUserFlags(userId)
            } catch (e: Exception) {
                handleFlaggingError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Private helper methods - each with single responsibility (SRP)

    private suspend fun saveFlag(userId: Int, placeId: String, photoId: String) {
        flagRepository.addFlag(userId, placeId, photoId)
    }

    private suspend fun checkForBadgeUnlocks(userId: Int, placeId: String) {
        val event = createExplorationEvent(userId, placeId)
        badgeRepository.logExploration(userId, event)
            .onSuccess { response -> handleNewBadges(response.newBadges) }
            .onFailure { Log.e("MapsViewModel", "Badge check failed", it) }
    }

    private fun createExplorationEvent(userId: Int, placeId: String): ExplorationEvent {
        // Find the place in our current places list to get its coordinates
        val place = _places.value.find { it.id == placeId }

        return ExplorationEvent(
            locationName = place?.displayName ?: placeId,
            latitude = place?.latitude,
            longitude = place?.longitude,
            notes = null
        )
    }

    private fun handleNewBadges(badges: List<Badge>) {
        if (badges.isNotEmpty()) {
            _newlyUnlockedBadges.value = badges
            _showBadgeDialog.value = true
        }
    }

    private suspend fun refreshUserFlags(userId: Int) {
        getFlags(userId)
    }

    private fun handleFlaggingError(e: Exception) {
        _error.value = e.localizedMessage ?: "Failed to mark location"
        Log.e("MapsViewModel", "Error marking place", e)
    }
    fun getFlags(userId: Int){
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = flagRepository.getFlags(userId);
                _userFullFlags.value = flagRepository.getFullFlags(userId)
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

    fun dismissBadgeDialog() {
        _showBadgeDialog.value = false
        _newlyUnlockedBadges.value = emptyList()
    }

    // Check for pending badge unlocks (called when returning to map from camera)
    fun checkPendingBadges() {
        val pendingBadges = PendingBadgeUnlocks.consume()
        if (pendingBadges.isNotEmpty()) {
            _newlyUnlockedBadges.value = pendingBadges
            _showBadgeDialog.value = true
        }
    }
}

