package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fontys.frontend.common.RegistrationView
import com.fontys.frontend.ui.viewmodels.LoginViewModel
import com.fontys.frontend.ui.viewmodels.RegistrationVIewModel

@Composable
fun RegistrationView(
    navController: NavHostController,
    viewModel: RegistrationVIewModel = viewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TextField(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            state = rememberTextFieldState()
        )

        TextField(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            state = rememberTextFieldState()
        )

        TextField(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            state = rememberTextFieldState()
        )

        Button(onClick = {}) {
            Text("Sign in")
        }


        Button(
            onClick = { navController.navigate(RegistrationView) }
        ) {
            Text("Sign up")
        }
    }
}