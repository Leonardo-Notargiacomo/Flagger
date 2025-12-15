package com.fontys.frontend.data.repositories

import com.fontys.frontend.data.remote.ApiClient
import com.fontys.frontend.ui.viewmodels.ReviewState
import kotlinx.coroutines.flow.MutableStateFlow

class ReviewRepository {

    private val dbApi = ApiClient.reviewApi

    fun postReview(reviewState: MutableStateFlow<ReviewState>) {
        try {

        }
        catch (e: Exception) {
            throw e
        }
    }
}