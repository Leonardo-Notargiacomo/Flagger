package com.fontys.frontend.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fontys.frontend.data.UserRegister
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.LoginViewModel
import com.fontys.frontend.ui.views.LoginView
import com.fontys.frontend.ui.views.NavBar
import com.fontys.frontend.ui.views.RegistrationView as RegistrationViewComposable


class MainActivity : ComponentActivity() {

    // Launcher for location permission
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Location permission granted, now check notification permission
                checkNotificationPermission()
            } else {
                // Location permission denied, but still check notification permission
                checkNotificationPermission()
            }
        }

    // Launcher for notification permission (Android 13+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Notification permission granted
            } else {
                // Notification permission denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Configure window to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Hide the navigation bar
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        checkLocationPermission()
        checkNotificationPermission()

        setContent {
            val navController = rememberNavController()

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = if(UserRepository.token.isEmpty()) "login" else "main"
                    ) {
                        composable("login") {
                            LoginView(navController)
                        }
                        composable("registration") {
                            RegistrationViewComposable(navController)
                        }
                        composable("main") {
                            NavBar()
                        }
                    }
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Location permission already granted, check notification permission
            checkNotificationPermission()
        }
    }

    private fun checkNotificationPermission() {
        // Only request notification permission on Android 13+ (TIRAMISU)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}




