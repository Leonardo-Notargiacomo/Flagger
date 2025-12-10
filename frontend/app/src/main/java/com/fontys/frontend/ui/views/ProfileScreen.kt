package com.fontys.frontend.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.UserUpdate
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.components.AccountField
import com.fontys.frontend.ui.components.EditableAccountField
import com.fontys.frontend.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userViewModel: ProfileViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {}
) {
    val user by userViewModel.user.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    var isEditing by remember { mutableStateOf(false) }
    var editEmail by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        userViewModel.getUser(UserRepository.userId.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Unknown error",
                        color = colorScheme.error
                    )
                }
            }

            user != null -> {
                val userData = user!!

                if (!isEditing && editEmail.isEmpty()) {
                    editEmail = userData.email ?: ""
                    editPassword = ""
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isEditing) {
                        EditableAccountField(
                            label = "Email",
                            value = editEmail,
                            onValueChange = { editEmail = it }
                        )
                    } else {
                        AccountField(
                            label = "Email",
                            value = userData.email ?: ""
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditing) {
                        PasswordField(
                            label = "Password",
                            password = editPassword,
                            onPasswordChange = { editPassword = it },
                            passwordVisible = passwordVisible,
                            onToggleVisibility = { passwordVisible = !passwordVisible }
                        )
                    } else {
                        AccountField(
                            label = "Password",
                            value = "••••••••",
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isEditing) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val userUpdate = UserUpdate(
                                            id = userData.id.toString(),
                                            userName = userData.userName,
                                            userImage = userData.userImage,
                                            bio = userData.bio,
                                            email = editEmail,
                                            password = editPassword.takeIf { it.isNotBlank() }
                                        )
                                        userViewModel.updateUser(userData.id.toString(), userUpdate)
                                        isEditing = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.secondary,
                                    contentColor = colorScheme.onSecondary
                                )
                            ) {
                                Text(
                                    "Save Changes",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            TextButton(
                                onClick = {
                                    isEditing = false
                                    editEmail = userData.email ?: ""
                                    editPassword = ""
                                    passwordVisible = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Cancel",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = colorScheme.onBackground
                                )
                            }
                        } else {
                            Button(
                                onClick = { isEditing = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.secondary,
                                    contentColor = colorScheme.onSecondary
                                )
                            ) {
                                Text(
                                    "Edit Account",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Text("Log out", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onDeleteAccount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.error
                        ),
                        border = BorderStroke(1.dp, colorScheme.error)
                    ) {
                        Text(
                            "Delete Account",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
            val contentDesc = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDesc
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
