package com.fontys.frontend.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Badge
import com.fontys.frontend.data.models.ExplorationEvent
import com.fontys.frontend.data.repositories.BadgeRepository
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

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    var front = false  // Start with back camera (matches bindToCamera default)
    var flash = true
    val flagRepository = FlagRepository()
    val badgeRepository = BadgeRepository()
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

        _isProcessing.value = true

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
                        try {
                            // Save the flag
                            flagRepository.addFlag(userid, placeId, toBase64(context, output.savedUri))

                            // Check for badge unlocks (wait for it to complete!)
                            checkForBadgeUnlocks(userid, placeId)

                            // Only navigate after badge check is done
                            onPhotoSaved(output.savedUri)
                        } finally {
                            _isProcessing.value = false
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    _isProcessing.value = false
                    onPhotoSaved(null)
                }
            }
        )
   }

    private suspend fun checkForBadgeUnlocks(userId: Int, placeId: String) {
        try {
            Log.d("CameraViewModel", "📍 Checking badges for user=$userId, place=$placeId")
            val event = ExplorationEvent(
                locationName = placeId,
                latitude = null,
                longitude = null,
                notes = null
            )
            val result = badgeRepository.logExploration(userId, event)

            result.onSuccess { response ->
                Log.d("CameraViewModel", "✅ API Success: ${response.newBadges.size} badges")
                if (response.newBadges.isNotEmpty()) {
                    Log.d("CameraViewModel", "🏆 Badges unlocked: ${response.newBadges.map { it.name }}")
                    PendingBadgeUnlocks.addBadges(response.newBadges)
                } else {
                    Log.d("CameraViewModel", "No new badges this time")
                }
            }
            result.onFailure { e ->
                Log.e("CameraViewModel", "❌ Badge check failed: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "💥 Exception checking badges", e)
        }
    }
}

// Shared object to pass badge unlocks between screens
object PendingBadgeUnlocks {
    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges

    fun addBadges(newBadges: List<Badge>) {
        _badges.value = newBadges
    }

    fun consume(): List<Badge> {
        val current = _badges.value
        _badges.value = emptyList()
        return current
    }
}
