package com.fontys.frontend.data.models



data class Review(
    val id: Int,
    val title: String,
    val desc: String,
    val rating: Double?
)