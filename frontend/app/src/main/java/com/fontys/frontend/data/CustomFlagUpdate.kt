package com.fontys.frontend.data

import com.google.gson.annotations.SerializedName
import java.util.Date

data class CustomFlagUpdate(
    @SerializedName("background")
    val background: String,
    @SerializedName("emoji")
    val emoji: String,
    @SerializedName("border")
    val border: String,
    @SerializedName("goUserId")
    val userId: Int
)
