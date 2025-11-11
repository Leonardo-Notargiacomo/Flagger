package com.fontys.frontend.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.repositories.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUiState(
    val friends: List<FriendListItem> = emptyList(),
    val receivedRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val searchResults: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val isLoadingFriends: Boolean = false,
    val isLoadingRequests: Boolean = false,
    val isSendingRequest: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class FriendsViewModel : ViewModel() {
    private val repository = FriendsRepository()

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    // Cache to store user data from search results and other sources
    private val userCache = mutableMapOf<Int, User>()

    companion object {
        private const val TAG = "FriendsViewModel"
    }

    // TODO: Replace hardcoded token with actual auth token from auth system
    // TODO: Auth developer - integrate with AuthRepository/TokenManager when ready
    // TODO: This should be set via setAuthToken() after user logs in
    private var authToken: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjIiLCJuYW1lIjoiTGVvIiwiZW1haWwiOiJsZW9AZ21haWwuY29tIiwiaWF0IjoxNzYyODUzMDY5LCJleHAiOjE3NjI4NzQ2Njl9.NpLukC1vOqcrdTYAoYGqs8L9mxg_RUsGEPl4d4h8xY0"

    // TODO: Get from auth system - hardcoded to match token above (id: "2")
    private var currentUserId: Int = 2

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun setCurrentUserId(userId: Int) {
        currentUserId = userId
    }

    fun searchUsers(query: String) {
        Log.d(TAG, "searchUsers() called with query: '$query'")
        if (query.isBlank()) {
            Log.d(TAG, "searchUsers() query is blank, clearing results")
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)

            repository.searchUsers(authToken, query).fold(
                onSuccess = { users ->
                    // Cache all users for later use
                    users.forEach { user ->
                        userCache[user.id] = user
                    }

                    // Filter out current user from search results
                    val filteredUsers = users.filter { it.id != currentUserId }
                    Log.d(TAG, "searchUsers() success: found ${users.size} users, cached ${users.size}, filtered to ${filteredUsers.size} (removed current user)")
                    _uiState.value = _uiState.value.copy(
                        searchResults = filteredUsers,
                        isSearching = false
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "searchUsers() failed: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = error.message ?: "Failed to search users"
                    )
                }
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFriends = true, error = null)

            repository.getFriends(authToken).fold(
                onSuccess = { friends ->
                    // Cache friend details
                    friends.forEach { friendItem ->
                        userCache[friendItem.friendId] = friendItem.friendDetails
                    }
                    Log.d(TAG, "loadFriends() cached ${friends.size} friend details")

                    _uiState.value = _uiState.value.copy(
                        friends = friends,
                        isLoadingFriends = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingFriends = false,
                        error = error.message ?: "Failed to load friends"
                    )
                }
            )
        }
    }

    fun loadReceivedRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRequests = true, error = null)

            repository.getReceivedRequests(authToken).fold(
                onSuccess = { requests ->
                    // Cache fromUser data from received requests
                    requests.forEach { request ->
                        request.fromUser?.let { user ->
                            userCache[user.id] = user
                        }
                    }
                    Log.d(TAG, "loadReceivedRequests() cached ${requests.count { it.fromUser != null }} users from received requests")

                    _uiState.value = _uiState.value.copy(
                        receivedRequests = requests,
                        isLoadingRequests = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingRequests = false,
                        error = error.message ?: "Failed to load received requests"
                    )
                }
            )
        }
    }

    fun loadSentRequests() {
        Log.d(TAG, "loadSentRequests() called")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRequests = true, error = null)

            repository.getSentRequests(authToken).fold(
                onSuccess = { requests ->
                    Log.d(TAG, "loadSentRequests() success: loaded ${requests.size} requests")

                    // Populate toUser from cache if null (backend bug workaround)
                    val enhancedRequests = requests.map { request ->
                        if (request.toUser == null && userCache.containsKey(request.toUserId)) {
                            val cachedUser = userCache[request.toUserId]
                            Log.d(TAG, "loadSentRequests() populated toUser from cache for userId=${request.toUserId}, userName=${cachedUser?.userName}")
                            request.copy(toUser = cachedUser)
                        } else {
                            request
                        }
                    }

                    enhancedRequests.forEachIndexed { index, request ->
                        Log.d(TAG, "loadSentRequests() request[$index]: toUserId=${request.toUserId}, toUser=${request.toUser}, userName=${request.toUser?.userName}")
                    }
                    _uiState.value = _uiState.value.copy(
                        sentRequests = enhancedRequests,
                        isLoadingRequests = false
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "loadSentRequests() failed: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isLoadingRequests = false,
                        error = error.message ?: "Failed to load sent requests"
                    )
                }
            )
        }
    }

    fun sendFriendRequest(toUserId: Int) {
        Log.d(TAG, "sendFriendRequest() called with toUserId: $toUserId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingRequest = true, error = null)

            repository.sendFriendRequest(authToken, toUserId).fold(
                onSuccess = { request ->
                    Log.d(TAG, "sendFriendRequest() success: request created with id=${request.id}")
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        successMessage = "Friend request sent successfully"
                    )
                    // Reload sent requests
                    Log.d(TAG, "sendFriendRequest() reloading sent requests...")
                    loadSentRequests()
                },
                onFailure = { error ->
                    Log.e(TAG, "sendFriendRequest() failed: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        error = error.message ?: "Failed to send friend request"
                    )
                }
            )
        }
    }

    fun acceptFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingRequest = true, error = null)

            repository.acceptFriendRequest(authToken, requestId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        successMessage = response.message
                    )
                    // Reload both friends and received requests
                    loadFriends()
                    loadReceivedRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        error = error.message ?: "Failed to accept friend request"
                    )
                }
            )
        }
    }

    fun rejectFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingRequest = true, error = null)

            repository.rejectFriendRequest(authToken, requestId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        successMessage = response.message
                    )
                    // Reload received requests
                    loadReceivedRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        error = error.message ?: "Failed to reject friend request"
                    )
                }
            )
        }
    }

    fun cancelFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingRequest = true, error = null)

            repository.cancelFriendRequest(authToken, requestId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        successMessage = "Friend request cancelled"
                    )
                    // Reload sent requests
                    loadSentRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        error = error.message ?: "Failed to cancel friend request"
                    )
                }
            )
        }
    }

    fun removeFriend(friendId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingRequest = true, error = null)

            repository.removeFriend(authToken, friendId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        successMessage = "Friend removed"
                    )
                    // Reload friends list
                    loadFriends()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        error = error.message ?: "Failed to remove friend"
                    )
                }
            )
        }
    }

    fun getFriendFlags(friendId: Int, onResult: (Result<List<Flag>>) -> Unit) {
        viewModelScope.launch {
            val result = repository.getFriendFlags(authToken, friendId)
            onResult(result)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
