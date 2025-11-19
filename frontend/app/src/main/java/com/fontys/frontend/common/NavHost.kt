package com.fontys.frontend.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.CameraPreviewViewModel
import com.fontys.frontend.ui.views.BadgeScreen
import com.fontys.frontend.ui.views.LoginView
import com.fontys.frontend.ui.views.MapsScreen
import com.fontys.frontend.ui.views.PictureCaptureScreen
import com.fontys.frontend.ui.views.ProfileScreen
import kotlinx.serialization.Serializable


@Serializable
object MapView

@Serializable
object FriendView

@Serializable
object ProfileView

@Serializable
object BadgeView

@Serializable
object LoginView

@Serializable
object RegistrationView

@Serializable
object CameraView

@Composable
fun NavHost(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = LoginView
    ) {
        composable<MapView> {
            MapsScreen(navController)
        }
        composable<FriendView> {
            //FriendView()
        }
        composable<ProfileView> {
            ProfileScreen()
        }
        composable<BadgeView> {
            // TODO: Get actual userId from auth system
            BadgeScreen(UserRepository.userId)
        }
        composable<LoginView> {
            LoginView(navController)
        }
        composable<RegistrationView> {
            //RegistrationView()
        }
        composable<CameraView> {
            val cameraViewModel: CameraPreviewViewModel = viewModel()
            PictureCaptureScreen(navController, cameraViewModel)
        }
    }
}