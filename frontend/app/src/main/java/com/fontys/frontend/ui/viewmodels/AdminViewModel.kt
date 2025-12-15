package com.fontys.frontend.ui.viewmodels

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.FlagResponse
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
    var users: List<UserReturn> = emptyList(),
    var flags: List<FlagResponse> = emptyList()
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

    fun base64ToImageBitmap(base64: String?): ImageBitmap? {
        // Only attempt to decode if we actually have a base64 string and it's not the sentinel value
        if (base64 == null || base64 == "no Image") return null

        return try {
            // If the string has a header like "data:image/png;base64,...", strip it
            val cleanBase64 = base64.substringAfter(",")

            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadFlags() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                _uiState.value.flags = AdminRepository.getFlags(20)
                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "fethcing flags failed",
                    isLoading = false
                )

            }

        }
    }

    fun filterUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                _uiState.value.users = AdminRepository.filterUsersBio()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "filtering users failed",
                    isLoading = false
                )
            }
        }
    }


}