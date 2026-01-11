package com.fontys.frontend.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fontys.frontend.ui.viewmodels.FlagSheetViewmodel

@Composable
fun ReviewView(
    navController: NavController,
    viewmodel: FlagSheetViewmodel
) {
    val reviewState by viewmodel.review.collectAsState()
    val reviewList by viewmodel.reviewList.collectAsState()
    val isLoading by viewmodel.isLoading.collectAsState()
    val error by viewmodel.error.collectAsState()
    var showPostReview by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(reviewList.reviews) { review ->
                ListItem(
                    headlineContent = { Text(review.title) },
                    supportingContent = {
                        Text("Rating: ${review.rating}/5.0")
                    }
                )
                HorizontalDivider()
            }
        }

        FloatingActionButton(
            onClick = { showPostReview = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add a review"
            )
        }

        if (isLoading) {
            CircularProgressIndicator()
        }
    }

    if (showPostReview) {
        PostReviewDialog(
            reviewState = reviewState,
            isLoading = isLoading,
            error = error,
            onDismiss = {
                showPostReview = false
            },
            onTitleChange = viewmodel::updateTitle,
            onReviewChange = viewmodel::updateReview,
            onRatingChange = viewmodel::updateRating,
            onSubmit = {
                viewmodel.postReview(
                    onSuccess = { showPostReview = false }
                )
            }
        )
    }
}

@Composable
fun PostReviewDialog(
    reviewState: com.fontys.frontend.ui.viewmodels.ReviewState,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onReviewChange: (String) -> Unit,
    onRatingChange: (Double) -> Unit,
    onSubmit: () -> Unit
) {
    val isFormValid = reviewState.title.isNotBlank() &&
            reviewState.review.isNotBlank()


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Write a Review") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    label = { Text("Title") },
                    value = reviewState.title,
                    onValueChange = {
                        if (it.length <= 100) onTitleChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    supportingText = {
                        Text("${reviewState.title.length}/100")
                    },
                    isError = reviewState.title.isBlank() && error != null
                )

                OutlinedTextField(
                    label = { Text("Write your Review!") },
                    value = reviewState.review,
                    onValueChange = {
                        if (it.length <= 500) onReviewChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    enabled = !isLoading,
                    supportingText = {
                        Text("${reviewState.review.length}/500")
                    }
                )

                RatingStars(
                    rating = reviewState.rating,
                    onRatingChange = onRatingChange,
                    enabled = !isLoading
                )

                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isLoading && isFormValid
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RatingStars(
    rating: Double = 0.0,
    onRatingChange: (Double) -> Unit = {},
    enabled: Boolean = true
) {
    Column {
        Text("Rating: ${"%.1f".format(rating)}/5.0")
        Row {
            repeat(5) { index ->
                Icon(
                    imageVector = if (index < rating.toInt())
                        Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.clickable(enabled = enabled) {
                        onRatingChange((index + 1).toDouble())
                    }
                )
            }
        }
    }
}