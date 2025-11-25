package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel

class ReviewViewModel : ViewModel() {

    private val review = MutableStateFlow()
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
}