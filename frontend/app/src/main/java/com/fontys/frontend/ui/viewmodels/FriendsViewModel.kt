package com.fontys.frontend.ui.viewmodels

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
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class FriendsViewModel : ViewModel() {
    private val repository = FriendsRepository()

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private var authToken: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjciLCJuYW1lIjoiVXNlciIsImVtYWlsIjoibm90QGxlby5jb20iLCJpYXQiOjE3NjI3NzY3NjYsImV4cCI6MTc2Mjc5ODM2Nn0.GWxH3NChPoeCpOO0rvBeIZ4aEljg_H1IiOYbpj0E0rQ"

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)

            repository.searchUsers(authToken, query).fold(
                onSuccess = { users ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = users,
                        isSearching = false
                    )
                },
                onFailure = { error ->
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getFriends(authToken).fold(
                onSuccess = { friends ->
                    _uiState.value = _uiState.value.copy(
                        friends = friends,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load friends"
                    )
                }
            )
        }
    }

    fun loadReceivedRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getReceivedRequests(authToken).fold(
                onSuccess = { requests ->
                    _uiState.value = _uiState.value.copy(
                        receivedRequests = requests,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load received requests"
                    )
                }
            )
        }
    }

    fun loadSentRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getSentRequests(authToken).fold(
                onSuccess = { requests ->
                    _uiState.value = _uiState.value.copy(
                        sentRequests = requests,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load sent requests"
                    )
                }
            )
        }
    }

    fun sendFriendRequest(toUserId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.sendFriendRequest(authToken, toUserId).fold(
                onSuccess = { request ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Friend request sent successfully"
                    )
                    // Reload sent requests
                    loadSentRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to send friend request"
                    )
                }
            )
        }
    }

    fun acceptFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.acceptFriendRequest(authToken, requestId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                    // Reload both friends and received requests
                    loadFriends()
                    loadReceivedRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to accept friend request"
                    )
                }
            )
        }
    }

    fun rejectFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.rejectFriendRequest(authToken, requestId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                    // Reload received requests
                    loadReceivedRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to reject friend request"
                    )
                }
            )
        }
    }

    fun cancelFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.cancelFriendRequest(authToken, requestId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Friend request cancelled"
                    )
                    // Reload sent requests
                    loadSentRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to cancel friend request"
                    )
                }
            )
        }
    }

    fun removeFriend(friendId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.removeFriend(authToken, friendId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Friend removed"
                    )
                    // Reload friends list
                    loadFriends()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
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
