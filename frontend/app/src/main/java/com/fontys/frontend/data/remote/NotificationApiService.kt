package com.fontys.frontend.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * API service for FCM token registration and notification dismissal tracking
 */
interface NotificationApiService {

    /**
     * Register or update FCM token for the current user
     * POST /api/users/{userId}/fcm-token
     */
    @POST("api/users/{userId}/fcm-token")
    suspend fun registerFcmToken(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: FcmTokenRequest
    ): Response<FcmTokenResponse>

    /**
     * Remove/deactivate FCM token (e.g., on logout)
     * DELETE /api/users/{userId}/fcm-token
     */
    @HTTP(method = "DELETE", path = "api/users/{userId}/fcm-token", hasBody = true)
    suspend fun removeFcmToken(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: FcmTokenRequest
    ): Response<FcmTokenResponse>

    /**
     * Report that user dismissed a notification
     * POST /api/users/{userId}/notifications/{notificationId}/dismiss
     */
    @POST("api/users/{userId}/notifications/{notificationId}/dismiss")
    suspend fun reportNotificationDismissal(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("notificationId") notificationId: String
    ): Response<NotificationDismissalResponse>
}

/**
 * Request body for FCM token registration
 */
data class FcmTokenRequest(
    val token: String,
    val platform: String = "android"
)

/**
 * Response for FCM token operations
 */
data class FcmTokenResponse(
    val success: Boolean,
    val message: String
)

/**
 * Response for notification dismissal
 */
data class NotificationDismissalResponse(
    val success: Boolean
)
