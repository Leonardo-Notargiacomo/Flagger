package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.fontys.frontend.ui.viewmodels.RegistrationViewModel




@Composable
fun RegistrationView(
    navController: NavHostController,
    viewModel: RegistrationViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var registrationSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Navigate to login when registration succeeds
    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            navController.navigate("login?registrationSuccess=true") {
                popUpTo("registration") { inclusive = true }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Bio") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            onClick = {
                errorMessage = null
                when {
                    email.isBlank() || username.isBlank() || password.isBlank() || bio.isBlank() -> {
                        errorMessage = "Please fill in all fields"
                    }
                    password != confirmPassword -> {
                        errorMessage = "Passwords do not match"
                    }
                    password.length < 8 -> {
                        errorMessage = "Password must be at least 8 characters"
                    }
                    !password.contains("/^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\da-zA-Z]).{8,}\$/gm") -> {
                        errorMessage = "Password must include at least one lowercase letter, one uppercase letter, one number, and one special character."
                    }
                    !email.contains("/(?:[a-z0-9!#\$%&'*+\\x2f=?^_`\\x7b-\\x7d~\\x2d]+(?:\\.[a-z0-9!#\$%&'*+\\x2f=?^_`\\x7b-\\x7d~\\x2d]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9\\x2d]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9\\x2d]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9\\x2d]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])/") -> {
                        errorMessage = "The email must follow a valid email format."
                    }
                    else -> {
                        isLoading = true
                        viewModel.onSignUp(
                            email = email,
                            userName = username,
                            password = password,
                            bio = bio,
                            onSuccess = {
                                isLoading = false
                                registrationSuccess = true
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    }
                }
            }
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Sign Up")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Already have an account?")

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(
            onClick = { navController.navigate("login") }
        ) {
            Text("Sign in")
        }
    }
}
