package com.fontys.frontend

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fontys.frontend.utils.ExploreNotificationManager
import com.fontys.frontend.utils.FCMTokenManager
import com.fontys.frontend.utils.NotificationPermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var notificationPermissionHelper: NotificationPermissionHelper
    private lateinit var notificationManager: ExploreNotificationManager

    companion object {
        private const val TAG = "MainActivity"
        private const val DAILY_REMINDERS_TOPIC = "daily_exploration_reminders"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize notification permission helper
        // Initialize notification components
        notificationPermissionHelper = NotificationPermissionHelper(this)
        notificationManager = ExploreNotificationManager(this)

        // Create notification channel (safe to call multiple times)
        notificationManager.createNotificationChannel()

        // Request notification permission if not granted
        if (!notificationPermissionHelper.isNotificationPermissionGranted()) {
            notificationPermissionHelper.requestNotificationPermission()
        } else {
            // Permission already granted, set up FCM
            setupFCM()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        notificationPermissionHelper.handlePermissionResult(
            requestCode,
            grantResults,
            onGranted = {
                Toast.makeText(this, "Daily exploration reminders enabled!", Toast.LENGTH_LONG).show()
                // Permission granted, set up FCM
                setupFCM()
            },
            onDenied = {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "You won't receive exploration reminders", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Set up Firebase Cloud Messaging
     * 1. Get FCM token (used to send notifications to this specific device)
     * 2. Subscribe to daily reminders topic (used to send notifications to all users)
     */
    private fun setupFCM() {
        // Get the FCM token
        FCMTokenManager.getCurrentToken(
            onTokenReceived = { token ->
                Log.d(TAG, "FCM Token obtained: $token")
                // FUTURE ENHANCEMENT: Send token to backend for personalized notifications
                // For now: Topic-based notifications work without storing tokens
                // Later: Implement with Retrofit + user authentication for targeted notifications
                Toast.makeText(this, "Ready to receive notifications!", Toast.LENGTH_SHORT).show()
            },
            onError = { exception ->
                Log.e(TAG, "Failed to get FCM token", exception)
                Toast.makeText(this, "Failed to set up notifications", Toast.LENGTH_SHORT).show()
            }
        )

        // All users subscribed to this topic will receive the same daily notifications
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