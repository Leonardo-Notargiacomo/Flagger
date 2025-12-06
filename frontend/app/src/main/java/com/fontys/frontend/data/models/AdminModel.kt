package com.fontys.frontend.data.models

//admin check model
data class AdminResponse(
    val isAdmin : Boolean
)

//warning and ban models
data class WarnMsg(
    val id : Int,
    val reason: String,
    val date: String
)

data class BanUser(
    val id : Int,
    val reason: String,
    val date: String
)