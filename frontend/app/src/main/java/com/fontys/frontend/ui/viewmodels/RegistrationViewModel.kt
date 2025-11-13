package com.fontys.frontend.ui.viewmodels

import android.R
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

    fun onSignUp(email: String, userName: String, password: String, bio: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                if (password != _uiState.value.passwordRepeat) {
                    isLoading = userRepository.register(email, userName, password, bio)

                    userRepository.whoAmIm()

                    _uiState.value = _uiState.value.copy(isLoading = false)
                }else{
                    throw Exception("Passwords do not match")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Login failed",
                    isLoading = false
                )
            }
        }
    }
}


