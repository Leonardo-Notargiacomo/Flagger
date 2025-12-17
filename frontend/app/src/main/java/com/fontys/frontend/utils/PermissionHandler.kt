package com.fontys.frontend.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHandler(
    private val activity: ComponentActivity,
    private val onAllPermissionsGranted: () -> Unit = {}
) {
    sealed class PermissionDialogState {
        object None : PermissionDialogState()
        object AskingLocation : PermissionDialogState()
        object AskingCamera : PermissionDialogState()
        object AskingNotification : PermissionDialogState()
        object LocationDenied : PermissionDialogState()
        object LocationPermanentlyDenied : PermissionDialogState()
        object CameraDenied : PermissionDialogState()
        object CameraPermanentlyDenied : PermissionDialogState()
        object NotificationDenied : PermissionDialogState()
        object NotificationPermanentlyDenied : PermissionDialogState()
    }

    var dialogState by mutableStateOf<PermissionDialogState>(PermissionDialogState.None)
        private set

    private var locationPermissionLauncher: ActivityResultLauncher<String>? = null
    private var cameraPermissionLauncher: ActivityResultLauncher<String>? = null
    private var notificationPermissionLauncher: ActivityResultLauncher<String>? = null

    fun initialize() {
        locationPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleLocationPermissionResult(isGranted)
        }

        cameraPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleCameraPermissionResult(isGranted)
        }

        notificationPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleNotificationPermissionResult(isGranted)
        }
    }

    fun checkPermissions() {
        checkLocationPermission()
    }

    fun checkPermissionsOnResume() {
        when (dialogState) {
            is PermissionDialogState.LocationPermanentlyDenied -> {
                if (isLocationPermissionGranted()) {
                    Log.d(TAG, "Location permission granted in Settings!")
                    dialogState = PermissionDialogState.None
                    checkCameraPermission()
                }
            }
            is PermissionDialogState.CameraPermanentlyDenied -> {
                if (isCameraPermissionGranted()) {
                    Log.d(TAG, "Camera permission granted in Settings!")
                    dialogState = PermissionDialogState.None
                    checkNotificationPermission()
                }
            }
            is PermissionDialogState.NotificationPermanentlyDenied -> {
                if (isNotificationPermissionGranted()) {
                    Log.d(TAG, "Notification permission granted in Settings!")
                    dialogState = PermissionDialogState.None
                    onAllPermissionsGranted()
                }
            }
            else -> {}
        }
    }

    private fun checkLocationPermission() {
        if (!isLocationPermissionGranted()) {
            dialogState = PermissionDialogState.AskingLocation
        } else {
            checkCameraPermission()
        }
    }

    private fun checkCameraPermission() {
        if (!isCameraPermissionGranted()) {
            dialogState = PermissionDialogState.AskingCamera
        } else {
            checkNotificationPermission()
        }
    }

    private fun checkNotificationPermission() {
        if (!isNotificationPermissionGranted()) {
            dialogState = PermissionDialogState.AskingNotification
        } else {
            onAllPermissionsGranted()
        }
    }

    private fun handleLocationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            dialogState = PermissionDialogState.None
            checkCameraPermission()
        } else {
            val isPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            dialogState = if (isPermanentlyDenied) {
                PermissionDialogState.LocationPermanentlyDenied
            } else {
                PermissionDialogState.LocationDenied
            }
        }
    }

    private fun handleCameraPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            dialogState = PermissionDialogState.None
            checkNotificationPermission()
        } else {
            val isPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            )

            dialogState = if (isPermanentlyDenied) {
                PermissionDialogState.CameraPermanentlyDenied
            } else {
                PermissionDialogState.CameraDenied
            }
        }
    }

    private fun handleNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            dialogState = PermissionDialogState.None
            onAllPermissionsGranted()
        } else {
            val isPermanentlyDenied = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                false
            }

            dialogState = if (isPermanentlyDenied) {
                PermissionDialogState.NotificationPermanentlyDenied
            } else {
                PermissionDialogState.NotificationDenied
            }
        }
    }

    fun requestLocationPermission() {
        locationPermissionLauncher?.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun requestCameraPermission() {
        cameraPermissionLauncher?.launch(Manifest.permission.CAMERA)
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    fun exitApp() {
        activity.finishAffinity()
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Notifications don't require permission on Android < 13
        }
    }

    companion object {
        private const val TAG = "PermissionHandler"
    }
}
