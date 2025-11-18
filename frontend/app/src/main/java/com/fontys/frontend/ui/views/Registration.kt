package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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

    var passwordsAllign: Boolean by remember { mutableStateOf(false) }

    var errors: Boolean by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
            label = { Text("Email") }
        )

        TextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().padding(top = 8.dp),
            label = { Text("Username") }
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().padding(top = 8.dp),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().padding(top = 8.dp),
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        TextField(
            value = bio,
            onValueChange = { bio = it },
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().padding(top = 8.dp),
            label = { Text("Bio") }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (passwordsAllign){
                    viewModel.onSignUp(email, username, password, bio)
                    if (viewModel.isLoading){
                        navController.navigate("main")
                    } else {
                        errors = true
                    }
                } else {
                    errors = true
                }
            }
        ) {
            Text("Sign Up")
        }

        if (errors){
            Text("Passwords do not match")
        }

        Text("Already have an account?")
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { navController.navigate("login")}
        ) {
            Text("Sign in")
        }
    }
}
