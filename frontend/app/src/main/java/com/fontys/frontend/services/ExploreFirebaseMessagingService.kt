package com.fontys.frontend.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.fontys.frontend.utils.ExploreNotificationManager
import com.fontys.frontend.data.remote.ApiClient
import com.fontys.frontend.data.remote.FcmTokenRequest
import com.fontys.frontend.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
     * Send the FCM token to backend server for personalized notifications
     */
    private fun sendTokenToServer(token: String) {
        // Only send if user is logged in
        if (UserRepository.userId == 0 || UserRepository.token.isEmpty()) {
            Log.d(TAG, "User not logged in, skipping token upload")
            return
        }

        Log.d(TAG, "Sending FCM token to backend for userId: ${UserRepository.userId}")

        // Send token to backend asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.notificationApi.registerFcmToken(
                    token = "Bearer ${UserRepository.token}",
                    userId = UserRepository.userId,
                    request = FcmTokenRequest(
                        token = token,
                        platform = "android"
                    )
                )

                if (response.isSuccessful) {
                    Log.d(TAG, "FCM token sent to backend successfully")
                } else {
                    Log.e(TAG, "Failed to send FCM token: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending FCM token to backend", e)
            }
        }
    }
}
