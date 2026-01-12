package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.fontys.frontend.data.remote.ApiClient
import com.fontys.frontend.data.remote.FcmTokenRequest
import com.fontys.frontend.utils.FCMTokenManager
import kotlinx.coroutines.Dispatchers

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private val userRepository = UserRepository

    //introduce email and password variables if they are required later

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun onLoginClick(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                userRepository.login(email,password)
                userRepository.whoAmIm()

                // Register FCM token after successful login
                registerFcmToken()

                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Login failed",
                    isLoading = false
                )
                onError(e.message ?: "Login failed")
            }
        }
    }

    /**
     * Register FCM token with backend after login
     */
    private fun registerFcmToken() {
        FCMTokenManager.getCurrentToken(
            onTokenReceived = { token ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val response = ApiClient.notificationApi.registerFcmToken(
                            token = "Bearer ${UserRepository.token}",
                            userId = UserRepository.userId,
                            request = FcmTokenRequest(
                                token = token,
                                platform = "android"
                            )
                        )

                        if (response.isSuccessful) {
                            Log.d("LoginViewModel", "FCM token registered successfully")
                        } else {
                            Log.e("LoginViewModel", "Failed to register FCM token: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("LoginViewModel", "Error registering FCM token", e)
                    }
                }
            },
            onError = { exception ->
                Log.e("LoginViewModel", "Failed to get FCM token", exception)
            }
        )
    }
}