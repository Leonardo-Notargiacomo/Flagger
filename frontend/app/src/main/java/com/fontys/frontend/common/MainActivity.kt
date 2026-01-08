package com.fontys.frontend.common

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.core.content.ContextCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import com.fontys.frontend.domain.AdminRepository
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.components.PermissionDialogs
import com.fontys.frontend.ui.theme.AppTheme
import com.fontys.frontend.ui.views.AdminScreen
import com.fontys.frontend.ui.views.LoginView
import com.fontys.frontend.ui.views.NavBar
import com.fontys.frontend.ui.views.OnboardingView
import com.fontys.frontend.ui.views.RegistrationView as RegistrationViewComposable
import com.fontys.frontend.utils.FCMTokenManager
import com.fontys.frontend.utils.OnboardingPreferences
import com.fontys.frontend.utils.PermissionHandler
import com.fontys.frontend.utils.ChallengePreferences

class MainActivity : ComponentActivity() {

    private lateinit var permissionHandler: PermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ChallengePreferences for persistent challenge timer storage
        ChallengePreferences.init(applicationContext)


        // Initialize permission handler
        permissionHandler = PermissionHandler(
            activity = this,
            onAllPermissionsGranted = {
                Log.d("MainActivity", "All permissions granted!")
            }
        )
        permissionHandler.initialize()

        // Install splash screen
        installSplashScreen()

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Configure window to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Style navigation and status bars to match app theme based on dark mode
        val isDarkMode = (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        window.navigationBarColor = if (isDarkMode) {
            Color.parseColor("#2D2420") // Dark brown for dark mode
        } else {
            Color.parseColor("#E8DCC4") // Cream for light mode
        }

        window.statusBarColor = if (isDarkMode) {
            Color.parseColor("#2D2420") // Dark brown for dark mode
        } else {
            Color.parseColor("#E8DCC4") // Cream for light mode
        }

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightNavigationBars = !isDarkMode
        windowInsetsController.isAppearanceLightStatusBars = !isDarkMode

        // Permissions will be requested when user reaches main app after login/signup

        setContent {
            // Create ImageLoader with GIF support
            val imageLoader = ImageLoader.Builder(this)
                .components {
                    add(ImageDecoderDecoder.Factory())
                }
                .build()

            val navController = rememberNavController()

            // Determine start destination
            val hasSeenOnboarding = OnboardingPreferences.hasSeenOnboarding(this)
            val startDestination = when {
                !hasSeenOnboarding -> "onboarding"
                UserRepository.token.isEmpty() -> "login"
                else -> "main"
            }

            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = if (UserRepository.token.isEmpty()) "onboarding" else "loader"
                    ) {
                        composable("loader") {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }

                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(100)
                                val isAdmin = AdminRepository.isAdmin()
                                val destination = if (isAdmin) "admin" else "main"
                                navController.navigate(destination) {
                                    popUpTo("loader") { inclusive = true }
                                }
                            }
                        }
                        composable("onboarding") {
                            OnboardingView(navController)
                            // Mark onboarding as seen when this composable is launched
                            OnboardingPreferences.setOnboardingSeen(this@MainActivity)
                        }
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
                            // Request permissions when user enters main app
                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                permissionHandler.checkPermissions()
                                subscribeToNotifications()
                            }
                            NavBar(rootNavController = navController)
                        }
                        composable("admin") {
                            AdminScreen(mainNavController = navController)
                        }
                    }

                    // Show permission dialogs based on permission handler state
                    PermissionDialogs(
                        permissionHandler = permissionHandler,
                        imageLoader = imageLoader
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permissions when returning from Settings
        permissionHandler.checkPermissionsOnResume()
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
