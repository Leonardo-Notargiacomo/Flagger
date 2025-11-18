package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fontys.frontend.ui.viewmodels.LoginViewModel

@Composable
fun LoginView(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
            label = { Text("Email") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().padding(top = 8.dp),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {viewModel.onLoginClick(email,password)}
        ) {
            Text("Sign in")
        }

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { navController.navigate("registration") }
        ) {
            Text("Sign up")
        }
    }
}
