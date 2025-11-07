package com.fontys.frontend.data.models

import com.google.gson.annotations.SerializedName

data class Flag(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("location_id")
    val locationId: String,

    @SerializedName("photoCode")
    val photoCode: String?,

    @SerializedName("dateTaken")
    val dateTaken: String,

    @SerializedName("notification")
    val notification: Int,

    @SerializedName("userId")
    val userId: Int
)
