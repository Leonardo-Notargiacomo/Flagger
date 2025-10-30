package com.fontys.frontend.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class FCMTokenManager {

    companion object {
        private const val TAG = "FCMTokenManager"

        /**
         * Retrieve the current FCM token
         * This token is used by your backend to send push notifications to this device
         */
        fun getCurrentToken(onTokenReceived: (String) -> Unit, onError: (Exception) -> Unit) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    Log.d(TAG, "FCM Token: $token")
                    onTokenReceived(token)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get FCM token", exception)
                    onError(exception)
                }
        }

        /**
         * Subscribe to a topic for receiving topic-based notifications
         * Example: All users subscribed to "daily_reminders" will get the same notification
         */
        fun subscribeToTopic(topic: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnSuccessListener {
                    Log.d(TAG, "Subscribed to topic: $topic")
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to subscribe to topic: $topic", exception)
                    onError(exception)
                }
        }

        /**
         * Unsubscribe from a topic
         */
        fun unsubscribeFromTopic(topic: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnSuccessListener {
                    Log.d(TAG, "Unsubscribed from topic: $topic")
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to unsubscribe from topic: $topic", exception)
                    onError(exception)
                }
        }
    }
}
