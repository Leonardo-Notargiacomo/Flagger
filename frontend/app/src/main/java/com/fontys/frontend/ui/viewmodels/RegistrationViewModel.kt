package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class RegistrationUiState(
    val email: String = "",
    val password: String = "",
    val passwordRepeat: String = "",

    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

class RegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()
    private val userRepository = UserRepository
    var isLoading = false

    fun onSignUp(
        email: String,
        userName: String,
        password: String,
        bio: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            isLoading = true
            try {
                val success = userRepository.register(userName, email, password, bio)
                if (success) {
                    isLoading = false
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                } else {
                    isLoading = false
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Registration failed. Please try again.")
                }
            } catch (e: Exception) {
                isLoading = false
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Registration failed",
                    isLoading = false
                )
                onError(e.message ?: "Registration failed")
            }
        }
    }
}


