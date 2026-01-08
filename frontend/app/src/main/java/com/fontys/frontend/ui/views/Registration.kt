package com.fontys.frontend.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import android.widget.Toast
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
    val context = LocalContext.current

    // Navigate to main after registration (user is logged in in UserRepository.register)
    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            Toast.makeText(context, "Account created", Toast.LENGTH_SHORT).show()
            navController.navigate("main") {
                popUpTo("registration") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Flag Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = "Flag",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Create your account",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Email", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Username", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Password", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Confirm Password", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Bio field
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = { Text("Bio (Optional)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(8.dp),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sign up button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading,
            onClick = {
                errorMessage = null
                when {
                    email.isBlank() || username.isBlank() || password.isBlank() -> {
                        errorMessage = "Please fill in all required fields"
                    }
                    password != confirmPassword -> {
                        errorMessage = "Passwords do not match"
                    }
                    password.length < 8 -> {
                        errorMessage = "Password must be at least 8 characters"
                    }
                    !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\da-zA-Z]).{8,}$".toRegex()) -> {
                        errorMessage = "Password must include at least one lowercase letter, one uppercase letter, one number, and one special character."
                    }
                    !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()) -> {
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
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Sign up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign in link
        TextButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account? Sign in",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
