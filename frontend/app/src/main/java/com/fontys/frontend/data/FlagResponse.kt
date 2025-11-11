// com.fontys.frontend.data/FlagResponse.kt (or FlagDto.kt if you prefer Data Transfer Object)
package com.fontys.frontend.data

import com.google.gson.annotations.SerializedName
import java.util.Date

data class FlagResponse(
    val id: Int,
    @SerializedName("location_id")
    val locationId: String,
    @SerializedName("photoCode")
    val photoCode: String?, // Nullable
    @SerializedName("dateTaken")
    val dateTaken: Date,
    @SerializedName("notification")
    val notification: Int?, // Nullable
    @SerializedName("userId")
    val userId: Int
)