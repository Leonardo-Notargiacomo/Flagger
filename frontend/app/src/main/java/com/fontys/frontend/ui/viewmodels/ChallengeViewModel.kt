package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.repositories.ChallengeRepository
import com.fontys.frontend.utils.ChallengePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChallengeViewModel : ViewModel() {

    companion object {
        // Shared state flows that persist across ViewModel recreations
        private val _sharedCanSelectChallenge = MutableStateFlow(true)
        private val _sharedTimeUntilNextSelection = MutableStateFlow<Long>(0L)

        private var sharedTimerJob: kotlinx.coroutines.Job? = null

        // Keep a weak reference to the active ViewModel instance for callbacks
        private var activeInstance: ChallengeViewModel? = null
        private var hasTriggeredRefreshOnExpiry = false

        init {
            startGlobalCooldownTimer()
        }

        private fun startGlobalCooldownTimer() {
            if (sharedTimerJob == null || sharedTimerJob?.isActive != true) {
                sharedTimerJob = kotlinx.coroutines.GlobalScope.launch {
                    while (true) {
                        val cachedChallengeStartTime = ChallengePreferences.getChallengeStartTime()

                        if (cachedChallengeStartTime > 0L) {
                            val currentTimeMillis = System.currentTimeMillis()
                            val elapsedMillis = currentTimeMillis - cachedChallengeStartTime
                            val cooldownDuration = 24 * 60 * 60 * 1000L
                            val remainingMillis = (cooldownDuration - elapsedMillis).coerceAtLeast(0L)

                            _sharedTimeUntilNextSelection.value = remainingMillis
                            _sharedCanSelectChallenge.value = remainingMillis <= 0L

                            if (remainingMillis <= 0L) {
                                ChallengePreferences.clearChallengeData()

                                if (!hasTriggeredRefreshOnExpiry) {
                                    hasTriggeredRefreshOnExpiry = true
                                    activeInstance?.refreshAvailableChallengesOnCooldownExpiry()
                                }
                            }
                        } else {
                            _sharedCanSelectChallenge.value = true
                            _sharedTimeUntilNextSelection.value = 0L
                            hasTriggeredRefreshOnExpiry = false // Reset flag when no active cooldown
                        }
                        kotlinx.coroutines.delay(1000)
                    }
                }
            }
        }

        fun registerInstance(instance: ChallengeViewModel) {
            activeInstance = instance
        }

        fun unregisterInstance(instance: ChallengeViewModel) {
            if (activeInstance == instance) {
                activeInstance = null
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

    val canSelectChallenge: StateFlow<Boolean> = _sharedCanSelectChallenge.asStateFlow()
    val timeUntilNextSelection: StateFlow<Long> = _sharedTimeUntilNextSelection.asStateFlow()

    init {
        registerInstance(this)
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister when ViewModel is destroyed
        unregisterInstance(this)
    }


    private fun refreshAvailableChallengesOnCooldownExpiry() {
        viewModelScope.launch {
            repository.getAvailableChallenges()
                .onSuccess { challenges ->
                    _availableChallenges.value = challenges
                }
                .onFailure { error ->
                }
        }
    }

    fun loadUserChallenges(userId: Int) {
        viewModelScope.launch {
            repository.getChallengeHistory()
                .onSuccess { challenges ->
                    val active = challenges.filter { !it.isCompleted }
                    _activeChallenges.value = active
                    _completedChallenges.value = challenges.filter { it.isCompleted }

                    // Check if there are any active challenges
                    if (active.isNotEmpty()) {
                        // User has an active challenge, find the most recent one
                        val mostRecentChallenge = active.maxByOrNull { it.startedAt ?: "" }
                        if (mostRecentChallenge != null) {
                            val cachedChallengeId = ChallengePreferences.getChallengeId()

                            // Only update cached time if this is a NEW challenge or first load
                            // This prevents the timer from resetting on every load
                            if (cachedChallengeId != mostRecentChallenge.id) {
                                val startTime = if (mostRecentChallenge.startedAt != null) {
                                    parseDateTime(mostRecentChallenge.startedAt)
                                } else {
                                    System.currentTimeMillis()
                                }
                                // Save to persistent storage
                                ChallengePreferences.saveChallengeStartData(startTime, mostRecentChallenge.id)
                            }
                        }
                    } else if (!ChallengePreferences.hasSavedChallenge()) {
                        // No active challenges and no saved challenge data, allow selection
                        _sharedCanSelectChallenge.value = true
                        _sharedTimeUntilNextSelection.value = 0L
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
            java.time.Instant.parse(dateString).toEpochMilli()
        } catch (_: Exception) {
            try {
                java.time.LocalDateTime.parse(dateString).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    fun loadAvailableChallenges() {
        viewModelScope.launch {
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
            repository.selectChallenge(challengeId)
                .onSuccess { userChallenge ->
                    // Immediately update the shared state to reflect locked status
                    val startTime = System.currentTimeMillis()
                    val cooldownDuration = 24 * 60 * 60 * 1000L
                    _sharedCanSelectChallenge.value = false
                    _sharedTimeUntilNextSelection.value = cooldownDuration

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

