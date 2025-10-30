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
import android.os.Build
import android.widget.Button
import android.widget.Toast
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.fontys.frontend.R
import com.fontys.frontend.utils.ExploreNotificationManager
import com.fontys.frontend.utils.FCMTokenManager
import com.fontys.frontend.utils.NotificationPermissionHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.MapStyleOptions



class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationPermissionHelper: NotificationPermissionHelper
    private lateinit var notificationManager: ExploreNotificationManager

    companion object {
        private const val TAG = "MapsActivity"
        private const val PERMISSION_REQUEST_CODE = 1
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        private const val DAILY_REMINDERS_TOPIC = "daily_exploration_reminders"
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

        // Initialize notification components (but don't request permission yet)
        notificationPermissionHelper = NotificationPermissionHelper(this)
        notificationManager = ExploreNotificationManager(this)
        notificationManager.createNotificationChannel()
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
                requestNotificationPermissionIfNeeded()
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
                // After location permission is granted, request notification permission
                requestNotificationPermissionIfNeeded()
            }
        }

        notificationPermissionHelper.handlePermissionResult(
            requestCode,
            grantResults,
            onGranted = {
                Toast.makeText(this, "Daily exploration reminders enabled!", Toast.LENGTH_LONG).show()
                setupFCM()
            },
            onDenied = {
                Toast.makeText(this, "You won't receive exploration reminders", Toast.LENGTH_SHORT).show()
            }
        )
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

    private fun requestNotificationPermissionIfNeeded() {
        if (!notificationPermissionHelper.isNotificationPermissionGranted()) {
            notificationPermissionHelper.requestNotificationPermission()
        } else {
            setupFCM()
        }
    }

    private fun setupFCM() {
        // Get the FCM token
        FCMTokenManager.getCurrentToken(
            onTokenReceived = { token ->
                Log.d(TAG, "FCM Token obtained: $token")
                Toast.makeText(this, "Ready to receive notifications!", Toast.LENGTH_SHORT).show()
            },
            onError = { exception ->
                Log.e(TAG, "Failed to get FCM token", exception)
                Toast.makeText(this, "Failed to set up notifications", Toast.LENGTH_SHORT).show()
            }
        )

        // Subscribe to daily reminders topic
        FCMTokenManager.subscribeToTopic(
            DAILY_REMINDERS_TOPIC,
            onSuccess = {
                Log.d(TAG, "Subscribed to daily reminders topic")
                Toast.makeText(
                    this,
                    "You'll receive daily exploration reminders!",
                    Toast.LENGTH_LONG
                ).show()
            },
            onError = { exception ->
                Log.e(TAG, "Failed to subscribe to topic", exception)
            }
        )
    }
}

