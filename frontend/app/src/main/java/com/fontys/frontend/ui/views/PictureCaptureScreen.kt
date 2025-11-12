package com.fontys.frontend.ui.views

import androidx.compose.runtime.Composable
import android.Manifest
import android.R.attr.identifier
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fontys.frontend.ui.viewmodels.CameraPreviewViewModel

@Composable
fun PictureCaptureScreen(navController: NavController,viewModel: CameraPreviewViewModel,

) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()

    // ✅ This effect gets cancelled when user navigates away
    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(context, lifecycleOwner)
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.releaseCamera()
        }
    }
    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = Modifier.fillMaxSize()
        )
    }
}