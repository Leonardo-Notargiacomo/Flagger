package com.fontys.frontend.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fontys.frontend.R
import com.fontys.frontend.common.MainActivity

class ExploreNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "explore_daily_reminders"
        const val CHANNEL_NAME = "Daily Exploration Reminders"
        const val NOTIFICATION_ID = 1001
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Create notification channel (required for Android 8+)
     * Channels let users control notification settings per category
     * HIGH importance allows notifications to show as heads-up when backend sets priority
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders to explore the world around you"
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show a notification with custom title and message
     * Used by Firebase Cloud Messaging to display push notifications
     */
    fun showCustomExplorationReminder(title: String, message: String) {
        // Create an intent to open MapsActivity when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(message)
                .setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
