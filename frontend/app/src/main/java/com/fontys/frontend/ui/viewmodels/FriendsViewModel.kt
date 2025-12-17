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

sealed class FriendsOperation {
    object None : FriendsOperation()
    object SendingRequest : FriendsOperation()
    object AcceptingRequest : FriendsOperation()
    object RejectingRequest : FriendsOperation()
    object CancellingRequest : FriendsOperation()
    object RemovingFriend : FriendsOperation()
}

data class FriendsUiState(
    val friends: List<FriendListItem> = emptyList(),
    val receivedRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val searchResults: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val isLoadingFriends: Boolean = false,
    val isLoadingRequests: Boolean = false,
    val currentOperation: FriendsOperation = FriendsOperation.None,
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
    private val userRepository = UserRepository

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "FriendsViewModel"
        private const val MIN_SEARCH_LENGTH = 3
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

        // Minimum length check for privacy/security
        if (query.length < MIN_SEARCH_LENGTH) {
            Log.d(TAG, "searchUsers() query too short (${query.length} < $MIN_SEARCH_LENGTH)")
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearching = false
            )
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

    /**
     * Generic helper function to fetch and enhance data with missing user details.
     * Reduces code duplication across loadFriends, loadReceivedRequests, and loadSentRequests.
     */
    private suspend fun <T> fetchAndEnhanceWithUserDetails(
        items: List<T>,
        getMissingUserId: (T) -> Int,
        hasUserDetails: (T) -> Boolean,
        enhanceItem: (T, User?) -> T
    ): List<T> {
        // Identify items with missing user data
        val missingUserIds = items
            .filter { !hasUserDetails(it) }
            .map { getMissingUserId(it) }
            .distinct()

        Log.d(TAG, "fetchAndEnhanceWithUserDetails() found ${missingUserIds.size} missing users: $missingUserIds")

        // Fetch missing users from API
        val fetchedUsers = mutableMapOf<Int, User>()
        missingUserIds.forEach { userId ->
            repository.getUserById(authToken, userId).fold(
                onSuccess = { user ->
                    fetchedUsers[user.id] = user
                    Log.d(TAG, "fetchAndEnhanceWithUserDetails() fetched user: ${user.userName} (id=${user.id})")
                },
                onFailure = { error ->
                    Log.e(TAG, "fetchAndEnhanceWithUserDetails() failed to fetch user $userId: ${error.message}")
                }
            )
        }

        // Enhance items with fetched data
        return items.map { item ->
            if (!hasUserDetails(item) && fetchedUsers.containsKey(getMissingUserId(item))) {
                enhanceItem(item, fetchedUsers[getMissingUserId(item)])
            } else {
                item
            }
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFriends = true, error = null)

            repository.getFriends(authToken).fold(
                onSuccess = { friends ->
                    Log.d(TAG, "loadFriends() loaded ${friends.size} friends")

                    val enhancedFriends = fetchAndEnhanceWithUserDetails(
                        items = friends,
                        getMissingUserId = { it.friendId },
                        hasUserDetails = { it.friendDetails != null },
                        enhanceItem = { friend, user -> friend.copy(friendDetails = user) }
                    )

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

                    val enhancedRequests = fetchAndEnhanceWithUserDetails(
                        items = requests,
                        getMissingUserId = { it.fromUserId },
                        hasUserDetails = { it.fromUser != null },
                        enhanceItem = { request, user -> request.copy(fromUser = user) }
                    )

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

                    val enhancedRequests = fetchAndEnhanceWithUserDetails(
                        items = requests,
                        getMissingUserId = { it.toUserId },
                        hasUserDetails = { it.toUser != null },
                        enhanceItem = { request, user -> request.copy(toUser = user) }
                    )

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
            _uiState.value = _uiState.value.copy(currentOperation = FriendsOperation.SendingRequest, error = null)

            repository.sendFriendRequest(authToken, toUserId).fold(
                onSuccess = { request ->
                    Log.d(TAG, "sendFriendRequest() success: request created with id=${request.id}")
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
                        successMessage = "Friend request sent successfully"
                    )
                    // Reload sent requests
                    Log.d(TAG, "sendFriendRequest() reloading sent requests...")
                    loadSentRequests()
                },
                onFailure = { error ->
                    Log.e(TAG, "sendFriendRequest() failed: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
                        error = error.message ?: "Failed to send friend request"
                    )
                }
            )
        }
    }

    fun acceptFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentOperation = FriendsOperation.AcceptingRequest, error = null)

            repository.acceptFriendRequest(authToken, requestId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
                        successMessage = response.message
                    )
                    // Reload both friends and received requests
                    loadFriends()
                    loadReceivedRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
                        error = error.message ?: "Failed to accept friend request"
                    )
                }
            )
        }
    }

    fun rejectFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentOperation = FriendsOperation.RejectingRequest, error = null)

            repository.rejectFriendRequest(authToken, requestId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
                        successMessage = response.message
                    )
                    // Reload received requests
                    loadReceivedRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
                        error = error.message ?: "Failed to reject friend request"
                    )
                }
            )
        }
    }

    fun cancelFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentOperation = FriendsOperation.CancellingRequest, error = null)

            repository.cancelFriendRequest(authToken, requestId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
                        successMessage = "Friend request cancelled"
                    )
                    // Reload sent requests
                    loadSentRequests()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
                        error = error.message ?: "Failed to cancel friend request"
                    )
                }
            )
        }
    }

    fun removeFriend(friendId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentOperation = FriendsOperation.RemovingFriend, error = null)

            repository.removeFriend(authToken, friendId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
                        successMessage = "Friend removed"
                    )
                    // Reload friends list
                    loadFriends()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        currentOperation = FriendsOperation.None,
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

    fun getReceivedRequestIdForUser(userId: Int): Int? {
        return _uiState.value.receivedRequests
            .firstOrNull { it.fromUserId == userId && it.status == "PENDING" }
            ?.id
    }
}
