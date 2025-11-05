package com.fontys.frontend.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fontys.frontend.ui.views.LoginView
import kotlinx.serialization.Serializable

@Serializable
object MapView

@Serializable
object FriendView

@Serializable
object UserView

@Serializable
object LoginView

@Serializable
object RegistrationView

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
            //MapsActivity()
        }
        composable<FriendView> {
            //FriendView()
        }
        composable<UserView> {
            //UserView()
        }
        composable<LoginView> {
            LoginView(navController)
        }
        composable<RegistrationView> {
            //RegistrationView()
        }
    }
}