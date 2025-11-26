package com.fontys.frontend.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.fontys.frontend.data.models.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class ReviewState(
    val id: Int = 0,
    val title: String = "",
    val desc: String = "",
    val rating: Double? = 0.0
)

class ReviewViewModel : ViewModel() {

    private val reviewState = MutableStateFlow(ReviewState())
    val review: StateFlow<ReviewState> = reviewState.asStateFlow()

    fun updateTitle(title: String) {
        reviewState.update { it.title = title }
    }
}