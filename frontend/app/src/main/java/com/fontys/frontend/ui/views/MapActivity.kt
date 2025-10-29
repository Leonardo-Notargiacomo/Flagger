package com.fontys.frontend.ui.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.fontys.frontend.databinding.ActivityMapsBinding
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.fontys.frontend.R
import com.fontys.frontend.data.PlaceService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.MapStyleOptions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException



class MapActivity : AppCompatActivity(), OnMapReadyCallback  {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        binding.btnZoomIn.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.btnZoomOut.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
        binding.btnAddMarker.setOnClickListener {
            if(hasLocationPermission()){
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        markTheSpot(currentLatLng);
                        }
                    }
                }
        }
        binding.btnRecenter.setOnClickListener {
            if (hasLocationPermission()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    }
                }
            } else {
                checkLocationPermission()
            }
        }
        val btnMapType = findViewById<Button>(R.id.btnMapType)

        btnMapType.setOnClickListener {
            mMap.mapType = when (mMap.mapType) {
                GoogleMap.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_SATELLITE
                GoogleMap.MAP_TYPE_SATELLITE -> GoogleMap.MAP_TYPE_TERRAIN
                GoogleMap.MAP_TYPE_TERRAIN -> GoogleMap.MAP_TYPE_HYBRID
                else -> GoogleMap.MAP_TYPE_NORMAL
            }
        }
        // Init fused location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        when {
            hasLocationPermission() -> {
                enableUserLocation()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showPermissionRationale()
            }

            else -> {
                checkLocationPermission()
            }
        }
    }
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            } else {
            }
        }
    }
    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        )
        if (hasLocationPermission()) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
    }
    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("We need your location to show where you are on the map.")
            .setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
    private fun markTheSpot(latlng: LatLng){
        val client = OkHttpClient()
        val jsonBody = JSONObject().apply {
            put("maxResultCount", 10)
            put("locationRestriction", JSONObject().apply {
                put("circle", JSONObject().apply {
                    put("center", JSONObject().apply {
                        put("latitude", latlng.latitude)
                        put("longitude", latlng.longitude)
                    })
                    put("radius", 10.0) // radius in meters
                })
            })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://places.googleapis.com/v1/places:searchNearby")
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", "AIzaSyA43OMJ6H8ComtRoUCLaRfMzGM2NmOMPog")
            .addHeader("X-Goog-FieldMask", "places.displayName,places.location,places.id,places.iconBackgroundColor,places.iconMaskBaseUri") // controls what fields are returned
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("PlacesAPI", "Request failed", e)

            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        println("Error: ${response.code}")
                        Log.e("PlacesAPI", "Response Body: ${response.body?.string()}")

                    } else {
                        val responseData = response.body?.string()
                        println("Response: $responseData")
                        if (responseData != null) {
                            try {
                                val json = JSONObject(responseData)
                                val placesArray = json.getJSONArray("places")
                                val list = arrayListOf<PlaceService>()
                                for (i in 0 until placesArray.length()) {
                                    val place = placesArray.getJSONObject(i)
                                    val placeId = place.getString("id")
                                    val displayNameObj = place.getJSONObject("displayName")
                                    val name = displayNameObj.getString("text")
                                    val iconMaskBaseUri = place.getString("iconMaskBaseUri");
                                    val iconBackgroundColor = place.getString("iconMaskBaseUri")
                                    val locationObj = place.getJSONObject("location")
                                    val lat = locationObj.getDouble("latitude")
                                    val lng = locationObj.getDouble("longitude")
                                    val dto = PlaceService(placeId,name,iconMaskBaseUri,iconBackgroundColor, lat,lng);
                                    list.add(dto);
//

//
//                                    Log.i("PlacesAPI", "Found: $name ($placeId) at $lat,$lng")
//                                    runOnUiThread {
//                                        val position = LatLng(lat, lng)
//                                        mMap.addMarker(MarkerOptions().position(position).title(name))
//                                    }
                                }
                                runOnUiThread {
                                    showLocsSelection(list)
                                }

                            } catch (e: Exception) {
                                Log.e("PlacesAPI", "Error parsing response", e)
                            }
                        }

                    }
                }
            }
        })
    }



    private fun showLocsSelection(places: List<PlaceService>){
        if (places.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("No places found")
                .setMessage("Try again or move to another location.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val placeNames = places.map { it.displayName }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select a place to mark")
            .setItems(placeNames) { _, which ->
                val selected = places[which]

                // Add marker for the chosen place
                val position = LatLng(selected.latitude, selected.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(selected.displayName)
                )

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

