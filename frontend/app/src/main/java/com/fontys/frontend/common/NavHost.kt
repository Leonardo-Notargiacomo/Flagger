package com.fontys.frontend.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.CameraPreviewViewModel
import com.fontys.frontend.ui.views.BadgeScreen
import com.fontys.frontend.ui.views.PictureCaptureScreen
import com.fontys.frontend.ui.views.FriendsScreen
import com.fontys.frontend.ui.views.LoginView
import com.fontys.frontend.ui.views.RegistrationView
import com.fontys.frontend.ui.views.DeleteAccountScreen
import com.fontys.frontend.ui.views.MapsScreen
import com.fontys.frontend.ui.views.ProfileScreen
import com.fontys.frontend.ui.views.Profile
import com.fontys.frontend.ui.viewmodels.ProfileViewModel
import kotlinx.serialization.Serializable
import com.fontys.frontend.ui.views.NavBar
import com.fontys.frontend.ui.views.PublicProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel

@Serializable
object MapView

@Serializable
object FriendView

@Serializable
object ProfileView

@Serializable
object BadgeView

@Serializable
object AccountView

@Serializable
object LoginView

@Serializable
object NavigationView

@Serializable
object RegistrationView

@Serializable
data class PublicProfileView(val userId: Int)

@Serializable
object CameraView

@Serializable
object DeleteAccountView

@Composable
fun NavHost(
    navController: NavHostController,
    padding: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        NavHost(
            navController = navController,
            startDestination = MapView
        ) {
            composable<MapView> {
                MapsScreen(navController)
            }

            composable<FriendView> {
                // TODO: Pass actual auth token to FriendsViewModel when auth system is integrated
                // Example: val viewModel: FriendsViewModel = viewModel()
                //          viewModel.setAuthToken(authToken)
                FriendsScreen(navController = navController)
            }

            composable<ProfileView> {
                val profileViewModel: ProfileViewModel = viewModel()
                Profile(
                    viewModel = profileViewModel,
                    onAccountClick = { navController.navigate(AccountView) },
                    onEditProfile = { navController.navigate(AccountView) },
                    onDeleteAccount = { navController.navigate(DeleteAccountView) }
                )
            }

            composable<BadgeView> {
                BadgeScreen(userId = UserRepository.userId)
            }

            composable<AccountView> {
                val profileViewModel: ProfileViewModel = viewModel()
                ProfileScreen(
                    userViewModel = profileViewModel,
                    onDeleteAccount = { navController.navigate(DeleteAccountView) }
                )
            }

            composable<LoginView> {
                LoginView(navController)
            }

            composable<RegistrationView> {
                RegistrationView(navController)
            }

            composable<NavigationView> {
                NavBar()
            }

            composable<CameraView> {
                val cameraViewModel: CameraPreviewViewModel = viewModel()
                PictureCaptureScreen(navController, cameraViewModel)
            }

            composable<DeleteAccountView> {
                DeleteAccountScreen(
                    onCancel = { navController.popBackStack() },
                    onConfirmDelete = {
                        // TODO: integrate actual delete logic
                        navController.popBackStack(ProfileView, inclusive = false)
                    }
                )
            }

            composable<PublicProfileView> { backStackEntry ->
            // Type-safe navigation with Kotlin serialization
            // The route parameters are automatically parsed from the path
            val args = backStackEntry.arguments
            val userId = args?.getInt("userId") ?: 0

            PublicProfileScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        }
    }
}
