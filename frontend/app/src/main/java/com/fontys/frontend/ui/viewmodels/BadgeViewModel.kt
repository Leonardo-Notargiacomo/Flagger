package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fontys.frontend.data.models.Badge
import com.fontys.frontend.data.repositories.BadgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BadgeUiState {
    object Loading : BadgeUiState()
    data class Success(
        val badges: List<Badge>,
        val totalBadges: Int,
        val earnedBadges: Int
    ) : BadgeUiState()
    data class Error(val message: String) : BadgeUiState()
}

class BadgeViewModel : ViewModel() {

    private val repository = BadgeRepository()

    private val _uiState = MutableStateFlow<BadgeUiState>(BadgeUiState.Loading)
    val uiState: StateFlow<BadgeUiState> = _uiState.asStateFlow()

    fun loadUserBadges(userId: Int) {
        viewModelScope.launch {
            _uiState.value = BadgeUiState.Loading

            repository.getUserBadges(userId).fold(
                onSuccess = { response ->
                    _uiState.value = BadgeUiState.Success(
                        badges = response.badges,
                        totalBadges = response.totalBadges,
                        earnedBadges = response.earnedBadges
                    )
                },
                onFailure = { error ->
                    _uiState.value = BadgeUiState.Error(
                        error.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }

    fun refreshBadges(userId: Int) {
        loadUserBadges(userId)
    }
}