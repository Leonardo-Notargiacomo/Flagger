package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.repositories.ChallengeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChallengeViewModel : ViewModel() {

    companion object {
        // Shared state flows that persist across ViewModel recreations
        private val _sharedCanSelectChallenge = MutableStateFlow(true)
        private val _sharedTimeUntilNextSelection = MutableStateFlow<Long>(0L)

        // Cache the challenge start time AND the challenge ID to persist across ViewModel recreations
        private var cachedChallengeStartTime: Long = 0L
        private var cachedChallengeId: Int = -1
        private var sharedTimerJob: kotlinx.coroutines.Job? = null

        // Start the global timer once
        init {
            startGlobalCooldownTimer()
        }

        private fun startGlobalCooldownTimer() {
            if (sharedTimerJob == null || sharedTimerJob?.isActive != true) {
                sharedTimerJob = kotlinx.coroutines.GlobalScope.launch {
                    while (true) {
                        if (cachedChallengeStartTime > 0L) {
                            val currentTimeMillis = System.currentTimeMillis()
                            val elapsedMillis = currentTimeMillis - cachedChallengeStartTime
                            val cooldownDuration = 24 * 60 * 60 * 1000L
                            val remainingMillis = (cooldownDuration - elapsedMillis).coerceAtLeast(0L)

                            _sharedTimeUntilNextSelection.value = remainingMillis
                            _sharedCanSelectChallenge.value = remainingMillis <= 0L

                            if (remainingMillis <= 0L) {
                                cachedChallengeStartTime = 0L
                            }
                        }
                        kotlinx.coroutines.delay(1000)
                    }
                }
            }
        }
    }

    private val repository = ChallengeRepository()

    private val _uiState = MutableStateFlow<ChallengeUiState>(ChallengeUiState.Loading)
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    private val _activeChallenges = MutableStateFlow<List<UserChallenge>>(emptyList())
    val activeChallenges: StateFlow<List<UserChallenge>> = _activeChallenges.asStateFlow()

    private val _completedChallenges = MutableStateFlow<List<UserChallenge>>(emptyList())
    val completedChallenges: StateFlow<List<UserChallenge>> = _completedChallenges.asStateFlow()

    private val _availableChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val availableChallenges: StateFlow<List<Challenge>> = _availableChallenges.asStateFlow()

    private val _showCompletionDialog = MutableStateFlow(false)
    val showCompletionDialog: StateFlow<Boolean> = _showCompletionDialog.asStateFlow()

    private val _completionData = MutableStateFlow<ChallengeCompletionResponse?>(null)
    val completionData: StateFlow<ChallengeCompletionResponse?> = _completionData.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Expose shared flows as instance properties
    val canSelectChallenge: StateFlow<Boolean> = _sharedCanSelectChallenge.asStateFlow()
    val timeUntilNextSelection: StateFlow<Long> = _sharedTimeUntilNextSelection.asStateFlow()

    fun loadUserChallenges(userId: Int) {
        viewModelScope.launch {
            // Use getChallengeHistory to get all user challenges
            repository.getChallengeHistory()
                .onSuccess { challenges ->
                    // Split into active and completed challenges
                    val active = challenges.filter { !it.isCompleted }
                    _activeChallenges.value = active
                    _completedChallenges.value = challenges.filter { it.isCompleted }

                    // Check if user can select a new challenge (24-hour cooldown)
                    if (active.isNotEmpty()) {
                        val mostRecentChallenge = active.maxByOrNull { it.startedAt ?: "" }
                        if (mostRecentChallenge != null) {
                            // Only update cached time if this is a NEW challenge or first load
                            // This prevents the timer from resetting on every load
                            if (cachedChallengeId != mostRecentChallenge.id) {
                                cachedChallengeId = mostRecentChallenge.id
                                cachedChallengeStartTime = if (mostRecentChallenge.startedAt != null) {
                                    parseDateTime(mostRecentChallenge.startedAt)
                                } else {
                                    System.currentTimeMillis()
                                }
                            }
                        }
                    } else {
                        _sharedCanSelectChallenge.value = true
                        _sharedTimeUntilNextSelection.value = 0L
                        cachedChallengeStartTime = 0L
                        cachedChallengeId = -1
                    }
                    
                    _uiState.value = ChallengeUiState.Success
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load challenges"
                    _uiState.value = ChallengeUiState.Error(error.message ?: "Unknown error")
                }
        }
    }



    private fun parseDateTime(dateString: String): Long {
        return try {
            // Try to parse ISO 8601 format (e.g., "2025-11-20T10:30:00Z")
            java.time.Instant.parse(dateString).toEpochMilli()
        } catch (_: Exception) {
            try {
                // Fallback to another format if needed
                java.time.LocalDateTime.parse(dateString).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    fun loadAvailableChallenges() {
        viewModelScope.launch {
            // Use getAvailableChallenges to get available challenges
            repository.getAvailableChallenges()
                .onSuccess { challenges ->
                    _availableChallenges.value = challenges
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load available challenges"
                }
        }
    }

    fun startChallenge(userId: Int, challengeId: Int) {
        viewModelScope.launch {
            // Use selectChallenge to start/select a challenge
            repository.selectChallenge(challengeId)
                .onSuccess { userChallenge ->
                    // Refresh the challenges list
                    loadUserChallenges(userId)
                    loadAvailableChallenges()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to start challenge"
                }
        }
    }

    fun updateChallengeProgress(userId: Int, challengeId: Int, progress: Int) {
        viewModelScope.launch {
            // Use checkChallengeCompletion to check/update progress
            repository.checkChallengeCompletion(mapOf("challengeId" to challengeId, "progress" to progress))
                .onSuccess { response ->
                    if (response.success && response.challenge.isCompleted) {
                        _completionData.value = response
                        _showCompletionDialog.value = true
                    }
                    // Refresh the challenges list
                    loadUserChallenges(userId)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to update challenge progress"
                }
        }
    }

    fun dismissCompletionDialog() {
        _showCompletionDialog.value = false
        _completionData.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

sealed class ChallengeUiState {
    object Loading : ChallengeUiState()
    object Success : ChallengeUiState()
    data class Error(val message: String) : ChallengeUiState()
}

