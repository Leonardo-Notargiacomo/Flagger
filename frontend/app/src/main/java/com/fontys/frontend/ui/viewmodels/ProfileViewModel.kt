package com.fontys.frontend.ui.viewmodels

import android.app.Application
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.data.UserUpdate
import com.fontys.frontend.data.models.ChangePasswordRequest
import com.fontys.frontend.data.models.Flag
import com.fontys.frontend.data.models.FlagShowData
import com.fontys.frontend.domain.MapRepository
import com.fontys.frontend.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository
    private val mapRepo = MapRepository()


    private val _user = MutableStateFlow<UserReturn?>(null)
    val user: StateFlow<UserReturn?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val flags = MutableStateFlow<List<Flag>?>(null)
    val flag: StateFlow<List<Flag>?> = flags

    private val flagNames = MutableStateFlow<List<FlagShowData>?>(null)
    val flagName: StateFlow<List<FlagShowData>?> = flagNames

    private val flagNr = MutableStateFlow<Int?>(flags.value?.size ?: 0)
    val flagNrs: StateFlow<Int?> = flagNr

    private val FriendsNr = MutableStateFlow<Int?>(0)
    val friendsNr: StateFlow<Int?> = FriendsNr

    private val _deleteAccountSuccess = MutableStateFlow(false)
    val deleteAccountSuccess: StateFlow<Boolean> = _deleteAccountSuccess


    init {
        this.getFriends()
    }

    fun getUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = userRepository.getUser(userId)
                if (result != null) {
                    _user.value = result
                    // Fetch flags after user data is loaded (getFlagNames will be called automatically after flags load)
                    getFlags(userId)
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

    fun updateUser(userId: String, userUpdate: UserUpdate, passwords: ChangePasswordRequest? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = userRepository.updateUser(userId, userUpdate, passwords)
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

    fun base64ToImageBitmap(base64: String?): ImageBitmap? {
        if (base64 == "no Image") {
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
        } else {
            return null
        }
    }

    fun getFlags(userId: String) {
        viewModelScope.launch {
            _error.value = null

            try {
                flags.value = userRepository.getFlag(userId)
                // Only call getFlagNames after flags are loaded
                getFlagNames()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching user", e)
                _error.value = e.localizedMessage ?: "Unknown error fetching Flags"
                _isLoading.value = false
            }
        }
    }

    fun getFriends() {
        viewModelScope.launch {
            _error.value = null

            try {
                val result = userRepository.getFriendsNr()
                if (result != null) {
                    FriendsNr.value = result
                } else {
                    _error.value = "Failed to fetch user data."
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching user", e)
                _error.value = e.localizedMessage ?: "Unknown error fetching Flags"
            }
        }
    }

    fun getFlagNames() {
        viewModelScope.launch {
            _error.value = null

            try {
                // Only fetch flag names if flags list is not null/empty
                if (flag.value.isNullOrEmpty()) {
                    flagNames.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                val result = mapRepo.getNames(flag.value)
                if (result.isSuccess) {
                    flagNames.value = result.getOrNull()
                    _isLoading.value = false
                } else {
                    _error.value = "Failed to fetch user data."
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching user", e)
                _error.value = e.localizedMessage ?: "Unknown error fetching user"
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _deleteAccountSuccess.value = false

            try {
                userRepository.deleteAccount(userId)
                UserRepository.token = ""
                UserRepository.userId = -1
                _deleteAccountSuccess.value = true
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error deleting user", e)
                _error.value = e.localizedMessage ?: "Unknown error deleting user"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearDeleteAccountSuccess() {
        _deleteAccountSuccess.value = false
    }
}
