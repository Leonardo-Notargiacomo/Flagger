package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String = ""
)

class LoginViewModel : ViewModel() {

    //introduce email and password variables if they are required later

    fun submitLogin(email: String, password: String) {
        
    }
}