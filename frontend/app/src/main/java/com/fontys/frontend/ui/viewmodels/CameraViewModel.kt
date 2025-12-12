package com.fontys.frontend.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.domain.FlagRepository
import com.fontys.frontend.domain.toBase64
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class CameraPreviewViewModel : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest
    private var  imageCapture: ImageCapture? = null;

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null


    private val _base64 = MutableStateFlow<String>("")
    val base64: StateFlow<String> = _base64

    var front = false  // Start with back camera (matches bindToCamera default)
    var flash = true
    val flagRepository = FlagRepository()
    private fun buildImageCaptureUseCase(): ImageCapture {
        return ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(if(flash) ImageCapture.FLASH_MODE_ON
                            else ImageCapture.FLASH_MODE_OFF)
            .build().also {
                imageCapture = it
            }
    }
    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.value = newSurfaceRequest
        }
    }

    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        cameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        camera = cameraProvider?.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            cameraPreviewUseCase,
            buildImageCaptureUseCase()
        )

    }

    fun releaseCamera() {
        cameraProvider?.unbindAll()
    }
    suspend fun cameraMode(appContext: Context, lifecycleOwner: LifecycleOwner){
        val provider = cameraProvider ?: ProcessCameraProvider.awaitInstance(appContext)
        cameraProvider = provider
        front = !front
        provider.unbindAll()
        val selector = if (front)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA
        val newImageCapture = buildImageCaptureUseCase()
        provider.bindToLifecycle(
            lifecycleOwner,
            selector,
            cameraPreviewUseCase,
            newImageCapture
        )
    }
    suspend fun flash(){

        camera?.cameraControl?.enableTorch(flash)

        flash = !flash
            imageCapture?.flashMode =
                if (flash) ImageCapture.FLASH_MODE_ON
                else ImageCapture.FLASH_MODE_OFF
        }
   suspend fun takePhoto(context: Context,userid : Int, placeId: String ,onPhotoSaved: (Uri?) -> Unit) {
        val imageCapture = imageCapture ?: return

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            File(
                context.cacheDir,
                "photo_${System.currentTimeMillis()}.jpg"
            )
        ).build()

        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewModelScope.launch {
                        flagRepository.addFlag(userid, placeId, toBase64(context, output.savedUri))
                    }
                    onPhotoSaved(output.savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    onPhotoSaved(null)
                }
            }
        )
   }
}
