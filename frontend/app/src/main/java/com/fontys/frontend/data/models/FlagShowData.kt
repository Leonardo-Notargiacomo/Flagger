package com.fontys.frontend.data.models

import com.google.gson.annotations.SerializedName

class FlagShowData(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String,
    @SerializedName("photoCode")
    val photoCode: String?,

    @SerializedName("dateTaken")
    val dateTaken: String,

    )