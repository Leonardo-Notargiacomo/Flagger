package com.fontys.frontend.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.models.Flag
import com.fontys.frontend.data.models.FriendRequest
import com.fontys.frontend.data.models.User
import com.fontys.frontend.data.repositories.FriendsRepository
import com.fontys.frontend.domain.MapRepository
import com.fontys.frontend.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State data class consolidating all profile-related state into a single source of truth.
 * This simplifies state management and makes it easier to update multiple properties atomically.
 */
data class PublicProfileUiState(
    val user: User? = null,
    val flags: List<Flag> = emptyList(),
    val flagDisplayNames: Map<String, String> = emptyMap(),
    val flagLocations: Map<String, Pair<Double, Double>> = emptyMap(),
    val friendRequestStatus: FriendRequestStatus = FriendRequestStatus.NotSent,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PublicProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val friendsRepository = FriendsRepository()
    private val mapRepository = MapRepository()

    // Single state flow consolidating all UI state
    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState: StateFlow<PublicProfileUiState> = _uiState.asStateFlow()

    // Legacy API compatibility - expose individual properties for backward compatibility
    val user: StateFlow<User?> = MutableStateFlow<User?>(null).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.user }
        }
    }
    val flags: StateFlow<List<Flag>> = MutableStateFlow<List<Flag>>(emptyList()).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.flags }
        }
    }
    val flagDisplayNames: StateFlow<Map<String, String>> = MutableStateFlow<Map<String, String>>(emptyMap()).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.flagDisplayNames }
        }
    }
    val flagLocations: StateFlow<Map<String, Pair<Double, Double>>> = MutableStateFlow<Map<String, Pair<Double, Double>>>(emptyMap()).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.flagLocations }
        }
    }
    val friendRequestStatus: StateFlow<FriendRequestStatus> = MutableStateFlow<FriendRequestStatus>(FriendRequestStatus.NotSent).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.friendRequestStatus }
        }
    }
    val isLoading: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.isLoading }
        }
    }
    val error: StateFlow<String?> = MutableStateFlow<String?>(null).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.error }
        }
    }

    companion object {
        private const val TAG = "PublicProfileViewModel"

        /**
         * Validates and retrieves the authentication token.
         * Returns null and logs error if token is not available.
         */
        private fun getAuthToken(): String? {
            return UserRepository.token.also {
                if (it == null) {
                    Log.e(TAG, "No authentication token available")
                }
            }
        }
    }

    fun loadUserProfile(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Get auth token
                val token = getAuthToken() ?: run {
                    _uiState.value = _uiState.value.copy(
                        error = "No authentication token",
                        isLoading = false
                    )
                    return@launch
                }

                // Load user data
                val userResult = friendsRepository.getUserById(token, userId)
                if (userResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load user profile",
                        isLoading = false
                    )
                    return@launch
                }

                val user = userResult.getOrNull()
                _uiState.value = _uiState.value.copy(user = user)

                // Load user's flags
                val flagsResult = friendsRepository.getFriendFlags(token, userId)
                if (flagsResult.isSuccess) {
                    val flagsList = flagsResult.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(flags = flagsList)

                    // Fetch place names for flags
                    if (flagsList.isNotEmpty()) {
                        fetchPlaceNames(flagsList)
                    }
                }

                // Check friend request status
                checkFriendRequestStatus(userId)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile", e)
                _uiState.value = _uiState.value.copy(
                    error = e.localizedMessage ?: "Unknown error"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun checkFriendRequestStatus(userId: Int) {
        try {
            val token = getAuthToken() ?: return

            // Check sent requests
            val sentRequestsResult = friendsRepository.getSentRequests(token)
            if (sentRequestsResult.isSuccess) {
                val sentRequests = sentRequestsResult.getOrNull() ?: emptyList()
                val pendingRequest = sentRequests.find {
                    it.toUserId == userId && it.status == "PENDING"
                }

                if (pendingRequest != null) {
                    _uiState.value = _uiState.value.copy(
                        friendRequestStatus = FriendRequestStatus.Pending(pendingRequest.id ?: 0)
                    )
                    return
                }
            }

            // Check if already friends
            val friendsResult = friendsRepository.getFriends(token)
            if (friendsResult.isSuccess) {
                val friends = friendsResult.getOrNull() ?: emptyList()
                val isFriend = friends.any { it.friendId == userId }

                if (isFriend) {
                    _uiState.value = _uiState.value.copy(
                        friendRequestStatus = FriendRequestStatus.Accepted
                    )
                    return
                }
            }

            // Not sent
            _uiState.value = _uiState.value.copy(
                friendRequestStatus = FriendRequestStatus.NotSent
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error checking friend request status", e)
        }
    }

    fun sendFriendRequest(userId: Int) {
        viewModelScope.launch {
            try {
                val token = getAuthToken() ?: run {
                    _uiState.value = _uiState.value.copy(error = "No authentication token")
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    friendRequestStatus = FriendRequestStatus.Sending
                )

                val result = friendsRepository.sendFriendRequest(token, userId)
                if (result.isSuccess) {
                    val request = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        friendRequestStatus = FriendRequestStatus.Pending(request?.id ?: 0)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to send friend request",
                        friendRequestStatus = FriendRequestStatus.NotSent
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending friend request", e)
                _uiState.value = _uiState.value.copy(
                    error = e.localizedMessage ?: "Unknown error",
                    friendRequestStatus = FriendRequestStatus.NotSent
                )
            }
        }
    }

    private fun fetchPlaceNames(flags: List<Flag>) {
        viewModelScope.launch {
            try {
                val locationIds = flags.map { it.locationId }
                val result = mapRepository.getLatlngs(locationIds)

                if (result.isSuccess) {
                    val flagDisplayList = result.getOrNull() ?: emptyList()
                    val namesMap = mutableMapOf<String, String>()
                    val locationsMap = mutableMapOf<String, Pair<Double, Double>>()

                    flagDisplayList.forEach { flagDisplay ->
                        namesMap[flagDisplay.locationId] = flagDisplay.displayName
                        locationsMap[flagDisplay.locationId] = Pair(
                            flagDisplay.location.latitude,
                            flagDisplay.location.longitude
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        flagDisplayNames = namesMap,
                        flagLocations = locationsMap
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching place names", e)
                // Don't show error to user, just keep location IDs
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

sealed class FriendRequestStatus {
    object NotSent : FriendRequestStatus()
    object Sending : FriendRequestStatus()
    data class Pending(val requestId: Int) : FriendRequestStatus()
    object Accepted : FriendRequestStatus()
}
