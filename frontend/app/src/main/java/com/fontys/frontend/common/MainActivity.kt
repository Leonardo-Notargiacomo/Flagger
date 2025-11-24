package com.fontys.frontend.common

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.components.LocationDeniedDialog
import com.fontys.frontend.ui.components.LocationSettingsDialog
import com.fontys.frontend.ui.components.NotificationDeniedDialog
import com.fontys.frontend.ui.components.NotificationSettingsDialog
import com.fontys.frontend.ui.components.PermissionDialog
import com.fontys.frontend.ui.views.LoginView
import com.fontys.frontend.ui.views.NavBar
import com.fontys.frontend.ui.views.RegistrationView as RegistrationViewComposable
import com.fontys.frontend.utils.FCMTokenManager

class MainActivity : ComponentActivity() {

    private var showLocationDialog by mutableStateOf(false)
    private var showNotificationDialog by mutableStateOf(false)
    private var showNotificationDeniedDialog by mutableStateOf(false)
    private var showNotificationSettingsDialog by mutableStateOf(false)
    private var notificationDenialCount = 0

    // Location denial dialogs
    private var showLocationDeniedDialog by mutableStateOf(false)
    private var showLocationSettingsDialog by mutableStateOf(false)
    private var locationDenialCount = 0

    // Launcher for location permission - keeps requesting until granted
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                // Check if permission is permanently denied (user can't be asked again)
                val isPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                showLocationDialog = false

                if (isPermanentlyDenied) {
                    // Permission permanently denied - show settings dialog
                    showLocationSettingsDialog = true
                    showLocationDeniedDialog = false
                } else {
                    // Permission denied but can ask again - show "come on" dialog
                    showLocationDeniedDialog = true
                    showLocationSettingsDialog = false
                }
            } else {
                // Permission granted - hide all location dialogs
                showLocationDialog = false
                showLocationDeniedDialog = false
                showLocationSettingsDialog = false
                locationDenialCount = 0
                // Location permission granted, now check notification permission
                checkNotificationPermission()
            }
        }

    // Launcher for notification permission - keeps requesting until granted
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                // Check if permission is permanently denied (user can't be asked again)
                val isPermanentlyDenied = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    false
                }

                showNotificationDialog = false

                if (isPermanentlyDenied) {
                    // Permission permanently denied - show settings dialog
                    showNotificationSettingsDialog = true
                    showNotificationDeniedDialog = false
                } else {
                    // Permission denied but can ask again - show "come on" dialog
                    showNotificationDeniedDialog = true
                    showNotificationSettingsDialog = false
                }
            } else {
                // Permission granted - hide all dialogs
                showNotificationDialog = false
                showNotificationDeniedDialog = false
                showNotificationSettingsDialog = false
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

        // Only check location permission first
        checkLocationPermission()

        // Subscribe to FCM topic for daily notifications
        subscribeToNotifications()

        setContent {
            // Create ImageLoader with GIF support
            val imageLoader = ImageLoader.Builder(this)
                .components {
                    add(ImageDecoderDecoder.Factory())
                }
                .build()

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
                        composable(
                            route = "login?registrationSuccess={registrationSuccess}",
                            arguments = listOf(
                                navArgument("registrationSuccess") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val registrationSuccess = backStackEntry.arguments?.getBoolean("registrationSuccess") ?: false
                            LoginView(navController, registrationSuccess = registrationSuccess)
                        }
                        composable("registration") {
                            RegistrationViewComposable(navController)
                        }
                        composable("main") {
                            NavBar()
                        }
                    }

                    // Show permission dialogs
                    if (showLocationDialog) {
                        PermissionDialog(
                            title = "🗺️ yo, where u at?",
                            message = "we need to know your location don't ask why 🤷\n\nbut if you do want to know here are the reasons:\n\n📍 we can help you find interesting spots around u\n\n🏠 we can check on you if you are just rotting at home\n\n🔥 and track ur adventure streak\n\nno cap, it's gonna be fire 🚀",
                            buttonText = "say less 💯",
                            gifUrl = "https://media.tenor.com/bGK0XXfceUoAAAAi/baby-pear.gif", // Replace with your GIF URL
                            imageLoader = imageLoader,
                            onConfirm = {
                                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        )
                    }

                    if (showNotificationDialog) {
                        PermissionDialog(
                            title = "🔔 don't ghost us!",
                            message = "turn on the notifications so we can:\n\n✨ slide into ur dms with daily inspo\n👥 tell u when u get friend requests\n🌱 remind you to touch some grass\n\ntrust, u don't wanna miss this 😤",
                            buttonText = "bet 🤝",
                            gifUrl = "https://media1.tenor.com/m/DxZ2AOxOIHEAAAAd/wacky-man.gif",
                            imageLoader = imageLoader,
                            onConfirm = {
                                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        )
                    }

                    // Show "come on" dialog when notification permission is denied
                    if (showNotificationDeniedDialog) {
                        NotificationDeniedDialog(
                            imageLoader = imageLoader,
                            onTryAgain = {
                                showNotificationDeniedDialog = false
                                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            onExitApp = {
                                finishAffinity() // Closes the app completely
                            }
                        )
                    }

                    // Show settings dialog when notification permission is permanently denied
                    if (showNotificationSettingsDialog) {
                        NotificationSettingsDialog(
                            imageLoader = imageLoader,
                            onOpenSettings = {
                                // Open app settings
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                startActivity(intent)
                            },
                            onExitApp = {
                                finishAffinity() // Closes the app completely
                            }
                        )
                    }

                    // Show "come on" dialog when location permission is denied
                    if (showLocationDeniedDialog) {
                        LocationDeniedDialog(
                            imageLoader = imageLoader,
                            onTryAgain = {
                                showLocationDeniedDialog = false
                                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            onExitApp = {
                                finishAffinity() // Closes the app completely
                            }
                        )
                    }

                    // Show settings dialog when location permission is permanently denied
                    if (showLocationSettingsDialog) {
                        LocationSettingsDialog(
                            imageLoader = imageLoader,
                            onOpenSettings = {
                                // Open app settings
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                startActivity(intent)
                            },
                            onExitApp = {
                                finishAffinity() // Closes the app completely
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check location permission when returning from Settings
        if (showLocationSettingsDialog) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission was granted in Settings!
                showLocationSettingsDialog = false
                Log.d("MainActivity", "Location permission granted in Settings!")
                // Now check notification permission
                checkNotificationPermission()
            }
        }

        // Re-check notification permission when returning from Settings
        if (showNotificationSettingsDialog) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission was granted in Settings!
                showNotificationSettingsDialog = false
                Log.d("MainActivity", "Notification permission granted in Settings!")
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showLocationDialog = true
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
            showNotificationDialog = true
        }
    }

    private fun subscribeToNotifications() {
        // Subscribe to the daily exploration reminders topic
        FCMTokenManager.subscribeToTopic(
            topic = "daily_exploration_reminders",
            onSuccess = {
                Log.d("MainActivity", "Successfully subscribed to daily_exploration_reminders")
            },
            onError = { exception ->
                Log.e("MainActivity", "Failed to subscribe to notifications", exception)
            }
        )
    }
}
