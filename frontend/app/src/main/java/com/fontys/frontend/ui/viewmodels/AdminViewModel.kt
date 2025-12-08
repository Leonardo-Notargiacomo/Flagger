package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.domain.AdminRepository
import com.fontys.frontend.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class AdminUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    var users: List<UserReturn> = emptyList()

)

class AdminViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()
    private val userRepository = UserRepository


    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                _uiState.value.users = AdminRepository.getRecentUsers(20)
                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                errorMessage = e.message ?: "fethcing users failed",
                isLoading = false
                )

            }

        }
    }


}