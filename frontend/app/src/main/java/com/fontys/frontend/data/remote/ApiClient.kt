package com.fontys.frontend.data.remote

import com.fontys.frontend.config.ApiConfig
import com.fontys.frontend.data.services.ReviewService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val badgeApi: BadgeApi = retrofit.create(BadgeApi::class.java)
    val friendsApi: FriendsApi = retrofit.create(FriendsApi::class.java)

    val reviewApi: ReviewService = retrofit.create(ReviewService::class.java)
}
