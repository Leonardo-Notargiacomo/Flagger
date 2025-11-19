package com.fontys.frontend.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.data.UserUpdate
import com.fontys.frontend.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository

    private val _user = MutableStateFlow<UserReturn?>(null)
    val user: StateFlow<UserReturn?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun getUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = userRepository.getUser(userId)
                if (result != null) {
                    _user.value = result
                } else {
                    _error.value = "Failed to fetch user data."
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching user", e)
                _error.value = e.localizedMessage ?: "Unknown error fetching user"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(userId: String, userUpdate: UserUpdate) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = userRepository.updateUser(userId, userUpdate)
                // Re-fetch the user data after successful update
                getUser(userId)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating user", e)
                _error.value = e.localizedMessage ?: "Unknown error updating user"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
