package com.fontys.frontend.domain

import android.util.Log
import com.fontys.frontend.config.ApiConfig
import com.fontys.frontend.data.FlagResponse
import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.domain.UserRepository.token
import com.fontys.frontend.services.AdminApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object AdminRepository {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    private val adminApiService = retrofit.create(AdminApiService::class.java)

    private fun getAuthHeaders(): Map<String, String> {
        return hashMapOf(
            "Accept" to "application/json",
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${UserRepository.token}"
        )
    }

    suspend fun isAdmin(): Boolean {
        return try {
            val response = adminApiService.isAdmin(getAuthHeaders(), UserRepository.userId)
            if (response.isSuccessful) {
                response.body() == true
            } else {
                Log.e("AdminRepository", "Error checking admin status: ${response.code()} ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("AdminRepository", "Error checking admin status", e)
            false
        }
    }

    suspend fun getRecentFlags(amount: Int): List<FlagResponse> {

    return emptyList()
    }

    suspend fun getRecentUsers(amount: Int): List<UserReturn> {
        try {
            val filter = " \"limit\" : $amount "
            val headers = HashMap<String, String>().apply {
                put("Accept", "application/json")
                put("Content-Type", "application/json")
                put("Authorization", "Bearer $token") ?: run {
                    throw IllegalStateException("JWT token is missing for authenticated request")
                }
            }
            val response = adminApiService.getUsers(headers, filter)
            if (response.isSuccessful){
                return response.body() ?: emptyList()
            } else {
                Log.e("AdminRepository", "Error getting recent users: ${response.code()} - ${response.message()}")
                return emptyList()
            }
        } catch (a: Exception) {
            Log.e("AdminRepository", "Exception getting recent users: ${a.message}", a)
            return emptyList()
        }

    }
}
