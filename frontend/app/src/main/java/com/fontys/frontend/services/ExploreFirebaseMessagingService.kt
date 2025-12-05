package com.fontys.frontend.services

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.fontys.frontend.utils.ExploreNotificationManager
import com.fontys.frontend.data.remote.ApiClient
import com.fontys.frontend.data.remote.FcmTokenRequest
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.receivers.NotificationDismissedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

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
            val notificationId = message.data["notificationId"] ?: UUID.randomUUID().toString()

            Log.d(TAG, "Notification - Title: $title, Body: $body, ID: $notificationId")

            // Show the notification with dismissal tracking
            showNotification(title, body, notificationId)
        }

        // Check if message contains data payload (custom data from backend)
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")

        }
    }

    /**
     * Display notification to user with dismissal tracking
     * High-priority FCM handles screen wake automatically
     */
    private fun showNotification(title: String, message: String, notificationId: String) {
        // Create delete intent to track when user dismisses the notification
        val deleteIntent = Intent(this, NotificationDismissedReceiver::class.java).apply {
            putExtra(NotificationDismissedReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationDismissedReceiver.EXTRA_USER_ID, UserRepository.userId)
        }

        val deletePendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId.hashCode(),
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show notification with delete tracking
        val notificationManager = ExploreNotificationManager(this)
        notificationManager.createNotificationChannel()
        notificationManager.showCustomExplorationReminderWithTracking(title, message, deletePendingIntent)
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
