package com.fontys.frontend.data.models

data class ChangePasswordRequest (
    var currentPassword: String = "",
    var newPassword: String = ""
)
