package com.fontys.frontend.ui.views


import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.SwitchCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    Box(modifier = Modifier.fillMaxSize()) {


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
                    viewModel.takePhoto(
                        context,
                        picturedata.currentUserId,
                        picturedata.place_id
                    ) { uri ->
                        picturedata.photoCode = viewModel.base64.value
                        navController.navigate(MapView)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .size(64.dp)
                .background(Color.Black, CircleShape),

            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Take picture",
                tint = Color.White,
                modifier = Modifier.size(32.dp)            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.cameraMode(context, lifecycleOwner)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
                .background(Color.Black, CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)



        ) {
            Icon(
                imageVector = Icons.Outlined.SwitchCamera,
                contentDescription = "Camera view change",
                tint = Color.White,

                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.flash()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .size(64.dp)
                .background(Color.Black, CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)



        ) {
            Icon(
                imageVector = if (viewModel.flash)
                    Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Camera view change",
                tint = Color.White,

                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}



