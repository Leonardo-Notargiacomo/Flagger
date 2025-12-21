package com.fontys.frontend.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fontys.frontend.ui.viewmodels.FlagSheetViewmodel

@Composable
fun ReviewView(
    navController: NavController,
    viewmodel: FlagSheetViewmodel = viewModel()
) {
    val reviewState by viewmodel.review.collectAsState()
    val reviewList by viewmodel.reviewList.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(reviewList.reviews) { review ->
                ListItem(
                    headlineContent = { reviewState.title }
                )
                HorizontalDivider()
            }
        }

        FloatingActionButton(
            onClick = {

            },
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add a review"
            )
        }
    }

    @Composable
    fun PostReview() {
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

@Composable
fun RatingStars() {
}