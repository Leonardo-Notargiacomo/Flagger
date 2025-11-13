package com.fontys.frontend.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.repositories.FriendsRepository
import com.fontys.frontend.domain.UserRepository
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

enum class RelationshipStatus {
    NONE,           // No relationship
    FRIENDS,        // Already friends
    PENDING_SENT,   // Sent request waiting for response
    PENDING_RECEIVED // Received request waiting for your response
}

class FriendsViewModel : ViewModel() {
    private val repository = FriendsRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "FriendsViewModel"
    }

    // Get auth token from UserRepository (populated after login)
    private val authToken: String
        get() = userRepository.token

    // Get current user ID from UserRepository (populated after login)
    private val currentUserId: Int
        get() = userRepository.userId

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
                    // Filter out current user from search results
                    val filteredUsers = users.filter { it.id != currentUserId }
                    Log.d(TAG, "searchUsers() success: found ${users.size} users, filtered to ${filteredUsers.size} (removed current user)")
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
                    Log.d(TAG, "loadFriends() loaded ${friends.size} friends")

                    // Identify friends with missing details
                    val missingUserIds = friends
                        .filter { it.friendDetails == null }
                        .map { it.friendId }
                        .distinct()

                    Log.d(TAG, "loadFriends() found ${missingUserIds.size} missing friend details: $missingUserIds")

                    // Fetch missing friend details from API
                    val fetchedUsers = mutableMapOf<Int, User>()
                    missingUserIds.forEach { userId ->
                        repository.getUserById(authToken, userId).fold(
                            onSuccess = { user ->
                                fetchedUsers[user.id] = user
                                Log.d(TAG, "loadFriends() fetched user: ${user.userName} (id=${user.id})")
                            },
                            onFailure = { error ->
                                Log.e(TAG, "loadFriends() failed to fetch user $userId: ${error.message}")
                            }
                        )
                    }

                    // Populate friendDetails with fetched data
                    val enhancedFriends = friends.map { friend ->
                        if (friend.friendDetails == null && fetchedUsers.containsKey(friend.friendId)) {
                            friend.copy(friendDetails = fetchedUsers[friend.friendId])
                        } else {
                            friend
                        }
                    }

                    enhancedFriends.forEachIndexed { index, friend ->
                        Log.d(TAG, "loadFriends() friend[$index]: friendId=${friend.friendId}, userName=${friend.friendDetails?.userName}")
                    }

                    _uiState.value = _uiState.value.copy(
                        friends = enhancedFriends,
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
                    Log.d(TAG, "loadReceivedRequests() success: loaded ${requests.size} requests")

                    // Identify requests with missing user data
                    val missingUserIds = requests
                        .filter { it.fromUser == null }
                        .map { it.fromUserId }
                        .distinct()

                    Log.d(TAG, "loadReceivedRequests() found ${missingUserIds.size} missing users: $missingUserIds")

                    // Fetch missing users from API
                    val fetchedUsers = mutableMapOf<Int, User>()
                    missingUserIds.forEach { userId ->
                        repository.getUserById(authToken, userId).fold(
                            onSuccess = { user ->
                                fetchedUsers[user.id] = user
                                Log.d(TAG, "loadReceivedRequests() fetched user: ${user.userName} (id=${user.id})")
                            },
                            onFailure = { error ->
                                Log.e(TAG, "loadReceivedRequests() failed to fetch user $userId: ${error.message}")
                            }
                        )
                    }

                    // Populate fromUser with fetched data
                    val enhancedRequests = requests.map { request ->
                        if (request.fromUser == null && fetchedUsers.containsKey(request.fromUserId)) {
                            request.copy(fromUser = fetchedUsers[request.fromUserId])
                        } else {
                            request
                        }
                    }

                    enhancedRequests.forEachIndexed { index, request ->
                        Log.d(TAG, "loadReceivedRequests() request[$index]: fromUserId=${request.fromUserId}, userName=${request.fromUser?.userName}, status=${request.status}")
                    }

                    // Filter to only show PENDING requests
                    val pendingRequests = enhancedRequests.filter { it.status == "PENDING" }
                    Log.d(TAG, "loadReceivedRequests() filtered to ${pendingRequests.size} PENDING requests (from ${enhancedRequests.size} total)")

                    _uiState.value = _uiState.value.copy(
                        receivedRequests = pendingRequests,
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

                    // Identify requests with missing user data
                    val missingUserIds = requests
                        .filter { it.toUser == null }
                        .map { it.toUserId }
                        .distinct()

                    Log.d(TAG, "loadSentRequests() found ${missingUserIds.size} missing users: $missingUserIds")

                    // Fetch missing users from API
                    val fetchedUsers = mutableMapOf<Int, User>()
                    missingUserIds.forEach { userId ->
                        repository.getUserById(authToken, userId).fold(
                            onSuccess = { user ->
                                fetchedUsers[user.id] = user
                                Log.d(TAG, "loadSentRequests() fetched user: ${user.userName} (id=${user.id})")
                            },
                            onFailure = { error ->
                                Log.e(TAG, "loadSentRequests() failed to fetch user $userId: ${error.message}")
                            }
                        )
                    }

                    // Populate toUser with fetched data
                    val enhancedRequests = requests.map { request ->
                        if (request.toUser == null && fetchedUsers.containsKey(request.toUserId)) {
                            request.copy(toUser = fetchedUsers[request.toUserId])
                        } else {
                            request
                        }
                    }

                    enhancedRequests.forEachIndexed { index, request ->
                        Log.d(TAG, "loadSentRequests() request[$index]: toUserId=${request.toUserId}, userName=${request.toUser?.userName}, status=${request.status}")
                    }

                    // Filter to only show PENDING requests
                    val pendingRequests = enhancedRequests.filter { it.status == "PENDING" }
                    Log.d(TAG, "loadSentRequests() filtered to ${pendingRequests.size} PENDING requests (from ${enhancedRequests.size} total)")

                    _uiState.value = _uiState.value.copy(
                        sentRequests = pendingRequests,
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

    // Helper function to determine relationship status with a user
    fun getRelationshipStatus(userId: Int): RelationshipStatus {
        val state = _uiState.value

        // Check if already friends
        if (state.friends.any { it.friendId == userId }) {
            return RelationshipStatus.FRIENDS
        }

        // Check if there's a pending sent request
        if (state.sentRequests.any { it.toUserId == userId && it.status == "PENDING" }) {
            return RelationshipStatus.PENDING_SENT
        }

        // Check if there's a pending received request
        if (state.receivedRequests.any { it.fromUserId == userId && it.status == "PENDING" }) {
            return RelationshipStatus.PENDING_RECEIVED
        }

        return RelationshipStatus.NONE
    }
}
