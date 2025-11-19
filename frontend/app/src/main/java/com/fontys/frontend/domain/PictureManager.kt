package com.fontys.frontend.domain

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.net.toFile
import java.io.File
import java.io.IOException


fun toBase64(context: Context, photoUri: Uri?): String {
    if (photoUri == null) return ""

    return try {
        val inputStream = context.contentResolver.openInputStream(photoUri)
        val bytes = inputStream?.readBytes() ?: return ""
        inputStream.close()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (error: IOException) {
        error.printStackTrace()
        ""
    }

}