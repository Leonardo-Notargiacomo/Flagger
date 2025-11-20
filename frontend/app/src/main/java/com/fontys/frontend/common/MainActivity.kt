package com.fontys.frontend.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.views.LoginView
import com.fontys.frontend.ui.views.NavBar
import com.fontys.frontend.ui.views.RegistrationView as RegistrationViewComposable
import com.fontys.frontend.utils.FCMTokenManager
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    private var showLocationDialog by mutableStateOf(false)
    private var showNotificationDialog by mutableStateOf(false)
    private var showNotificationDeniedDialog by mutableStateOf(false)
    private var showNotificationSettingsDialog by mutableStateOf(false)
    private var notificationDenialCount = 0

    // Launcher for location permission - keeps requesting until granted
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                showLocationDialog = false
                // Location permission granted, now check notification permission
                checkNotificationPermission()
            } else {
                // Location permission denied, show dialog and ask again
                showLocationDialog = true
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

                    // Show settings dialog when permission is permanently denied
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
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
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

@Composable
fun PermissionDialog(
    title: String,
    message: String,
    buttonText: String,
    gifUrl: String,
    imageLoader: ImageLoader,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // GIF Image
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(gifUrl)
                        .build(),
                    imageLoader = imageLoader
                )

                Image(
                    painter = painter,
                    contentDescription = "Permission GIF",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )

                // Message
                Text(
                    text = message,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )

                // Button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationDeniedDialog(
    imageLoader: ImageLoader,
    onTryAgain: () -> Unit,
    onExitApp: () -> Unit
) {
    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title with random humorous message
                val titles = listOf(
                    "😭 come on...",
                    "💔 really?",
                    "🥺 we hope that was an accident",
                    "😢 nah fr?",
                    "🤨 you serious rn?",
                    "😤 don't do us like that",
                    "🫠 this is awkward..."
                )
                val randomTitle = titles.random()

                Text(
                    text = randomTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // GIF Image - sad/disappointed GIF
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://media.tenor.com/-2rbVbnfW24AAAAi/crying-cat-sad-kitty.gif")
                        .build(),
                    imageLoader = imageLoader
                )

                Image(
                    painter = painter,
                    contentDescription = "Sad GIF",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )

                // Message
                val messages = listOf(
                    "we really hope that was a mistake 🥺\n\nwithout notifications we can't:\n\n💬 tell u when friends accept ur requests\n🎮 send u challenges\n🔥 remind u to keep ur streak alive\n\nit's gonna be sad without u...",
                    "bestie... we need those notifications 😢\n\nlook, without them:\n\n❌ no friend request alerts\n❌ no daily reminders\n❌ no achievement unlocks\n\nit's giving 'i hate fun' energy ngl",
                    "okay so like... what just happened? 🤔\n\nwe can't function without notifications:\n\n📱 no updates when ur friends vibe with u\n✨ no motivation to explore\n🏆 no achievement notifications\n\npls reconsider 🙏"
                )
                val randomMessage = messages.random()

                Text(
                    text = randomMessage,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )

                // Two buttons: Try Again (primary) and Exit App (destructive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exit App button (destructive/secondary)
                    OutlinedButton(
                        onClick = onExitApp,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "exit app 😔",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Try Again button (primary)
                    Button(
                        onClick = onTryAgain,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "try again 🙏",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsDialog(
    imageLoader: ImageLoader,
    onOpenSettings: () -> Unit,
    onExitApp: () -> Unit
) {
    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                val titles = listOf(
                    "😩 okay we get it...",
                    "🫤 so it's like that huh",
                    "💀 you really said no twice",
                    "😮‍💨 alright bet...",
                    "🤷 if you say so..."
                )
                val randomTitle = titles.random()

                Text(
                    text = randomTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // GIF Image - more intense sad/crying GIF
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://media1.tenor.com/m/lx2WSGRk8bcAAAAC/pulp-fiction-john-travolta.gif")
                        .build(),
                    imageLoader = imageLoader
                )

                Image(
                    painter = painter,
                    contentDescription = "Confused GIF",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )

                // Message - explain they need to go to Settings
                Text(
                    text = "you denied it twice so now android won't let us ask again 💔\n\nbut fr we NEED notifications to work properly:\n\n⚙️ you gotta go to Settings manually\n🔔 turn on notifications there\n📱 then come back here\n\nit's the only way this works bestie 🙏",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )

                // Two buttons: Go to Settings (primary) and Exit App (destructive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exit App button (destructive/secondary)
                    OutlinedButton(
                        onClick = onExitApp,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "nah i'm out 👋",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Open Settings button (primary)
                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "open settings ⚙️",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
