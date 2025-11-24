package com.fontys.frontend.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.net.toFile
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
fun fromBase64(string: String) : BitmapDescriptor? {
    if (string.isEmpty() || string==null) return BitmapDescriptorFactory.defaultMarker()

    val bytes  = Base64.decode(string,Base64.NO_WRAP)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 370, 320, false)

    return  BitmapDescriptorFactory.fromBitmap(scaledBitmap)
}