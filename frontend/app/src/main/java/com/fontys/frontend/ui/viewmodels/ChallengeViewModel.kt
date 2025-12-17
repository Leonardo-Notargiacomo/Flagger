package com.fontys.frontend.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.models.*
import com.fontys.frontend.data.repositories.ChallengeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChallengeViewModel : ViewModel() {

    private val repository = ChallengeRepository()

    private val _uiState = MutableStateFlow<ChallengeUiState>(ChallengeUiState.Loading)
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    private val _status = MutableStateFlow<ChallengeStatusResponse?>(null)
    val status: StateFlow<ChallengeStatusResponse?> = _status.asStateFlow()

    private val _history = MutableStateFlow<List<UserChallenge>>(emptyList())
    val history: StateFlow<List<UserChallenge>> = _history.asStateFlow()

    private val _showCompletionDialog = MutableStateFlow(false)
    val showCompletionDialog: StateFlow<Boolean> = _showCompletionDialog.asStateFlow()

    private val _completionData = MutableStateFlow<ChallengeCompletionResult?>(null)
    val completionData: StateFlow<ChallengeCompletionResult?> = _completionData.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val activeChallenge: StateFlow<UserChallenge?> = _status.map { it?.activeChallenge }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val availableChallenges: StateFlow<List<Challenge>> = _status.map { it?.availableChallenges.orEmpty() }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val isOnCooldown: StateFlow<Boolean> = _status.map { it?.isOnCooldown ?: false }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val cooldownEndsAt: StateFlow<String?> = _status.map { it?.cooldownEndsAt }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun refresh() {
        viewModelScope.launch {
            Log.d("ChallengeViewModel", "refresh() called")
            _uiState.value = ChallengeUiState.Loading
            val statusResult = repository.getChallengeStatus()
            val historyResult = repository.getChallengeHistory()

            statusResult.onSuccess {
                Log.d("ChallengeViewModel", "Status received - activeChallenge: ${it.activeChallenge?.challenge?.name}, progressData: ${it.activeChallenge?.progressData}")
                _status.value = it
            }
                .onFailure { _errorMessage.value = it.message }

            historyResult.onSuccess { _history.value = it }
                .onFailure { _errorMessage.value = it.message }

            _uiState.value = if (statusResult.isSuccess && historyResult.isSuccess) {
                ChallengeUiState.Success
            } else {
                ChallengeUiState.Error(_errorMessage.value ?: "Failed to load challenges")
            }
        }
    }

    fun selectChallenge(challengeId: Int) {
        viewModelScope.launch {
            repository.selectChallenge(challengeId)
                .onSuccess {
                    _status.value = _status.value?.copy(
                        hasActiveChallenge = true,
                        activeChallenge = it.userChallenge,
                        isOnCooldown = false,
                        cooldownEndsAt = it.cooldownEndsAt,
                        availableChallenges = emptyList()
                    )
                    refresh()
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun checkCompletion(onBadgeUnlocked: (() -> Unit)? = null) {
        viewModelScope.launch {
            Log.d("ChallengeViewModel", "checkCompletion() called")
            repository.checkChallengeCompletion()
                .onSuccess {
                    Log.d("ChallengeViewModel", "Completion check result: completed=${it.completed}, badge=${it.badge?.name}")
                    if (it.completed) {
                        _completionData.value = it
                        _showCompletionDialog.value = true
                        onBadgeUnlocked?.invoke()
                    }
                    refresh()
                }
                .onFailure { _errorMessage.value = it.message }
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
