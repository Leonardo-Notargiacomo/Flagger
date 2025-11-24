package com.fontys.frontend.ui.views


import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fontys.frontend.common.MapView
import com.fontys.frontend.ui.viewmodels.CameraPreviewViewModel
import com.fontys.frontend.ui.viewmodels.MapsViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
object picturedata{
    var photoCode = ""
    var place_id = ""
    var currentUserId = 0
    fun photoCode() = photoCode
}


@Composable
fun PictureCaptureScreen(navController: NavHostController, viewModel: CameraPreviewViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val base64 by viewModel.base64.collectAsStateWithLifecycle()
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()




    LaunchedEffect(lifecycleOwner) {
        // Delay ensures SurfaceView is fully ready before binding
        delay(150)
        viewModel.bindToCamera(context, lifecycleOwner)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.releaseCamera()
        }
    }

    surfaceRequest?.let {
        CameraXViewfinder(
            surfaceRequest = it,
            modifier = Modifier.fillMaxSize()
        )
    }
    val coroutineScope = rememberCoroutineScope()
    Button(
        onClick = {
            coroutineScope.launch {
                    viewModel.takePhoto(context, picturedata.currentUserId,
                        picturedata.place_id) { uri ->
                        println("📸 Saved at: $uri")
                        // Example navigation after saving:
                        val newBase64 = viewModel.base64.value ?: ""
                        picturedata.photoCode = newBase64
                        navController.navigate(MapView)
                }

            }
        },
        modifier = Modifier
            .padding(24.dp)
    ) {
        Text("Capture")
    }
}



