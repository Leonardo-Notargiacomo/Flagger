package com.fontys.frontend.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fontys.frontend.data.remote.ApiClient
import com.fontys.frontend.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that handles notification dismissal events
 * Tracks when users dismiss notifications to help improve notification targeting
 */
class NotificationDismissedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationDismissed"
        const val EXTRA_NOTIFICATION_ID = "notificationId"
        const val EXTRA_USER_ID = "userId"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
        val userId = intent.getIntExtra(EXTRA_USER_ID, 0)

        Log.d(TAG, "Notification dismissed: notificationId=$notificationId, userId=$userId")

        // Validate inputs
        if (notificationId == null || notificationId.isEmpty()) {
            Log.e(TAG, "Invalid notification ID")
            return
        }

        if (userId == 0) {
            Log.e(TAG, "Invalid user ID")
            return
        }

        // Report dismissal to backend
        reportDismissal(userId, notificationId)
    }

    /**
     * Report notification dismissal to backend
     */
    private fun reportDismissal(userId: Int, notificationId: String) {
        // Use pendingResult to keep receiver alive during async operation
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.notificationApi.reportNotificationDismissal(
                    token = "Bearer ${UserRepository.token}",
                    userId = userId,
                    notificationId = notificationId
                )

                if (response.isSuccessful) {
                    Log.d(TAG, "Dismissal reported successfully for notification $notificationId")
                } else {
                    Log.e(TAG, "Failed to report dismissal: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reporting dismissal for notification $notificationId", e)
            } finally {
                // Finish the async operation
                pendingResult.finish()
            }
        }
    }
}
