package com.fontys.frontend.domain

import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.data.UserUpdate
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.*
import com.fontys.frontend.services.UserAPIService


class UserRepository {
    val BASE_URL = "https://group-repository-2025-android-1-6of2.onrender.com/"
    var token ="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjEiLCJuYW1lIjoiZ2FuZ3N0YWxrZWQiLCJlbWFpbCI6Ind3d0B3dy53dyIsImlhdCI6MTc2MjQxNjk0MSwiZXhwIjoxNzYyNDM4NTQxfQ.xowNshI30rRavngwql8eIJ59NADVvpsHGXtXaPXJyFQ"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient =  OkHttpClient.Builder()
        // No custom auth interceptor needed here if using @HeaderMap directly
        .addInterceptor(loggingInterceptor) // Keep logging for debugging
        .build()

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // Use the OkHttpClient
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    private val userApiService = retrofit.create(UserAPIService::class.java)
    suspend fun getUser(userId: String) : UserReturn? {
         try {
            val headers = HashMap<String, String>().apply {
                put("Accept", "application/json")
                put("Content-Type", "application/json")
                token?.let { token ->
                    put("Authorization", "Bearer $token") // Add JWT token if available
                } ?: run {
                    // Optional: Log a warning or throw an error if token is missing for authenticated endpoint
                    // throw IllegalStateException("JWT token is missing for authenticated request")
                }
            }
            val response = userApiService.getUser(headers, userId)
            if (response.isSuccessful) {
                val userData = response.body()?: return null
                val id = userData.id
                val userName = userData.userName
                val email = userData.email
                val userImage = userData.userImage
                val bio = userData.bio
                return UserReturn(id, userName, email, userImage, bio)
            } else {
                // Handle error response
                println("Error: ${response.code()} - ${response.message()}")
                return null
            }
        } catch (
            e: Exception
        ) {
            // Handle exceptions such as network errors
            println("Exception: ${e.message}")
            return null
        }
    }
    
    suspend fun updateUser(userId: String, userUpdate: UserUpdate): String? {
        return try {
            val headers = HashMap<String, String>().apply {
                put("Accept", "application/json")
                put("Content-Type", "application/json")
                token?.let { token ->
                    put("Authorization", "Bearer $token") // Add JWT token if available
                } ?: run {
                    // Optional: Log a warning or throw an error if token is missing for authenticated endpoint
                    // throw IllegalStateException("JWT token is missing for authenticated request")
                }
            }
            val response = userApiService.updateUser(headers , userId, userUpdate)
            if (response.isSuccessful) {
                response.body()
            } else {
                "Error: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            "Exception: ${e.message}"
        }
    }
}