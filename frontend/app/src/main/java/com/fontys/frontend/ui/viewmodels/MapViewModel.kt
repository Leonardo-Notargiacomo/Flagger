package com.fontys.frontend.ui.viewmodels


import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.PlaceService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val client = OkHttpClient()

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

    fun fetchNearbyPlaces(latlng: LatLng) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val jsonBody = JSONObject().apply {
                put("maxResultCount", 10)
                put("locationRestriction", JSONObject().apply {
                    put("circle", JSONObject().apply {
                        put("center", JSONObject().apply {
                            put("latitude", latlng.latitude)
                            put("longitude", latlng.longitude)
                        })
                        put("radius", 10.0)
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://places.googleapis.com/v1/places:searchNearby")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Goog-Api-Key", "YOUR_API_KEY")
                .addHeader(
                    "X-Goog-FieldMask",
                    "places.displayName,places.location,places.id,places.iconMaskBaseUri,places.iconBackgroundColor"
                )
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    _isLoading.value = false
                    _error.value = e.localizedMessage ?: "Network error"
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.use {
                        _isLoading.value = false
                        if (!response.isSuccessful) {
                            _error.value = "Error: ${response.code}"
                            return
                        }

                        val responseData = response.body?.string() ?: return
                        try {
                            val json = JSONObject(responseData)
                            val placesArray = json.getJSONArray("places")
                            val list = mutableListOf<PlaceService>()
                            for (i in 0 until placesArray.length()) {
                                val place = placesArray.getJSONObject(i)
                                val id = place.getString("id")
                                val name = place.getJSONObject("displayName").getString("text")
                                val iconUri = place.getString("iconMaskBaseUri")
                                val color = place.getString("iconBackgroundColor")
                                val loc = place.getJSONObject("location")
                                val lat = loc.getDouble("latitude")
                                val lng = loc.getDouble("longitude")
                                list.add(PlaceService(id, name, iconUri, color, lat, lng))
                            }
                            _places.value = list
                        } catch (e: Exception) {
                            _error.value = "Parse error: ${e.message}"
                        }
                    }
                }
            })
        }
    }
}
