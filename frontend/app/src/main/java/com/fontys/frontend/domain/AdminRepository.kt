package com.fontys.frontend.domain

import android.util.Log
import com.fontys.frontend.config.ApiConfig
import com.fontys.frontend.data.FlagResponse
import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.services.AdminApiService
import com.fontys.frontend.services.FlagApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object AdminRepository {
    var token = ""
    var userId = 0
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        // No custom auth interceptor needed here if using @HeaderMap directly
        .addInterceptor(loggingInterceptor) // Keep logging for debugging
        .build()

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient) // Use the OkHttpClient
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    private val adminApiService = retrofit.create(AdminApiService::class.java)

    suspend fun isAdmin(): Boolean {
        val userId = UserRepository.userId
        val headers = HashMap<String, String>().apply {
            put("Accept", "application/json")
            put("Content-Type", "application/json")
            token.let { token ->
                put("Authorization", "Bearer $token")
            }
        }
        val response = adminApiService.isAdmin(headers, userId)
        try {
        if (response.isSuccessful) {
            val adminData = response.body()
            return adminData == true
        } else {
            return false
        } }
        catch (e: Exception) {
            Log.e("AdminRepository", "Error checking admin status", e)
            return false
        }
    }

    suspend fun getRecentFlags(amount: Int): Result<List<FlagResponse>> {
        // This creates a filter to get the 'amount' most recent flags, sorted by creation date
        val filter = """{"limit":$amount, "order":"dateTaken DESC"}"""
        val headers = HashMap<String, String>().apply {
            put("Accept", "application/json")
            put("Content-Type", "application/json")
            token.let { token ->
                put("Authorization", "Bearer $token")
            }
        }
        return try {
            val response = adminApiService.getFlags(headers, filter)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get flags"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentUsers(amount: Int): Result<List<UserReturn>> {
        // This creates a filter to get the 'amount' most recent flags, sorted by creation date
        val filter = """{"limit":$amount, "order":"dateTaken DESC"}"""
        val headers = HashMap<String, String>().apply {
            put("Accept", "application/json")
            put("Content-Type", "application/json")
            token.let { token ->
                put("Authorization", "Bearer $token")
            }
        }
        return try {
            val response = adminApiService.getUsers(headers, filter)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get flags"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}