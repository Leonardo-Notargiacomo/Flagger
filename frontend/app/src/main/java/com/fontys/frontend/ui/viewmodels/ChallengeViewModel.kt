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

    fun loadUserChallenges(userId: Int) {
        viewModelScope.launch {
            _uiState.value = ChallengeUiState.Loading

            // Use getChallengeHistory to get all user challenges
            repository.getChallengeHistory()
                .onSuccess { challenges ->
                    // Split into active and completed challenges
                    _activeChallenges.value = challenges.filter { !it.isCompleted }
                    _completedChallenges.value = challenges.filter { it.isCompleted }
                    _uiState.value = ChallengeUiState.Success
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load challenges"
                    _uiState.value = ChallengeUiState.Error(error.message ?: "Unknown error")
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

