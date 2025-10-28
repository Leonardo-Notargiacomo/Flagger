package com.fontys.frontend.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.fontys.frontend.utils.ExploreNotificationManager

/**
 * Firebase Messaging Service to handle push notifications from FCM
 * This service receives notifications even when app is in background
 */
class ExploreFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    /**
     * Called when a new FCM token is generated
     * This happens on first app install or when token is refreshed
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")

        // FUTURE ENHANCEMENT: Send token to backend for personalized notifications
        // Currently: Topic-based notifications work without this
        // Later: Implement when Retrofit + user authentication is ready
        sendTokenToServer(token)
    }

    /**
     * Called when a push notification is received from FCM
     * This is triggered when your backend sends a notification
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Check if message contains a notification payload
        message.notification?.let { notification ->
            val title = notification.title ?: "Time to Explore!"
            val body = notification.body ?: "Discover something new today!"

            Log.d(TAG, "Notification - Title: $title, Body: $body")

            // Show the notification using our notification manager
            showNotification(title, body)
        }

        // Check if message contains data payload (custom data from backend)
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")

        }
    }

    /**
     * Display notification to user
     * High-priority FCM handles screen wake automatically
     */
    private fun showNotification(title: String, message: String) {
        val notificationManager = ExploreNotificationManager(this)
        notificationManager.createNotificationChannel()
        notificationManager.showCustomExplorationReminder(title, message)
    }

    /**
     * Send the FCM token to your backend server
     * FUTURE ENHANCEMENT: For personalized notifications per user
     */
    private fun sendTokenToServer(token: String) {
        // FUTURE ENHANCEMENT: Implement when Retrofit is set up
        // Will require:
        // 1. Retrofit API client
        // 2. User authentication to link token to user ID
        // 3. Backend endpoint: POST /users/{userId}/fcm-token

        Log.d(TAG, "Token available for future backend integration: $token")

        // Currently: Topic-based notifications work without storing tokens
        // All users subscribed to "daily_exploration_reminders" receive notifications
    }
}
