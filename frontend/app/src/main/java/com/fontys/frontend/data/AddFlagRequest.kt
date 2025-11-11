package com.fontys.frontend.data

import android.app.Notification
import com.google.gson.annotations.SerializedName
import java.util.Date

data class AddFlagRequest(
    @SerializedName("location_id") // Use SerializedName if your Kotlin property name differs
    val locationId: String,
    @SerializedName("dateTaken")
    val dateTaken: Date,
    @SerializedName("notification")
    val notification: Int?, // Make nullable to omit if null
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("photoCode")
    val photoCode: String? // Make nullable to omit if null
)