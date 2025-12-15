package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fontys.frontend.ui.viewmodels.ReviewViewModel

@Composable
fun ReviewView(
    viewmodel: ReviewViewModel = viewModel(),
    navController: NavController
) {
    val reviewState by viewmodel.review.collectAsState()

    Box() {
        Column() {
            OutlinedTextField(
                label = { Text("Title") },
                value = reviewState.title,
                onValueChange = { viewmodel.updateTitle(it) }
            )

            OutlinedTextField(
                label = { Text("Write your Review!") },
                value = reviewState.review,
                onValueChange = { viewmodel.updateReview(it) }
            )

            RatingStars()
        }
    }
}

@Composable
fun RatingStars() {}