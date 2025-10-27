package com.fontys.frontend.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fontys.frontend.ui.views.UserView
import kotlinx.serialization.Serializable

@Serializable
object UserView

@Composable
fun NavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = UserView
    ) {
        composable<UserView> {
            UserView()
        }
    }
}