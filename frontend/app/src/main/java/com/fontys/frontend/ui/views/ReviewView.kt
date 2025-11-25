package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.ui.viewmodels.ReviewViewModel

@Composable
fun ReviewView(
    viewmodel: ReviewViewModel = viewModel()
) {
    Box() {
        TextField(
            label = "Your review",
            value = viewmodel,
            onValueChange = TODO()
        )
    }
}