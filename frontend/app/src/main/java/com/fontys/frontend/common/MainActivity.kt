package com.fontys.frontend.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.fontys.frontend.data.UserRegister
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.LoginViewModel
import com.fontys.frontend.ui.views.LoginView
import com.fontys.frontend.ui.views.NavBar


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // The user granted permission — Compose screen can load location
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        var navController = NavHostController(this)

        super.onCreate(savedInstanceState)

        checkLocationPermission()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if(UserRepository.token.isEmpty()){
                        LoginView(navController)
                    }else{
                        NavBar()
                    }
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}




