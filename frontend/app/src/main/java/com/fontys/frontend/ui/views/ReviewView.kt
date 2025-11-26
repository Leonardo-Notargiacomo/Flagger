package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.ui.viewmodels.ReviewViewModel

@Composable
fun ReviewView(
    viewmodel: ReviewViewModel = viewModel()
) {
    val reviewState by viewmodel.review.collectAsState()

    Box() {
        OutlinedTextField(
            label = "Title",
            value = reviewState.title,
            onValueChange = reviewState.updateTitle()
        )
    }
}