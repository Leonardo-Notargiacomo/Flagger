package com.fontys.frontend.domain

import android.util.Log
import com.fontys.frontend.data.UserLogin
import com.fontys.frontend.data.UserReturn
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.*
import com.fontys.frontend.domain.UserAPIService



object UserRepository {

    val BASE_URL = "https://group-repository-2025-android-1.onrender.com/"
    var token =""
    var userId=0
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
    suspend fun login(email : String, password: String) {
        try {
            val headers = HashMap<String, String>().apply {
                put("Accept", "application/json")
                put("Content-Type", "application/json")
                 ?: run {
                    // Optional: Log a warning or throw an error if token is missing for authenticated endpoint
                    // throw IllegalStateException("JWT token is missing for authenticated request")
                }
            }
            val response = userApiService.login(headers, UserLogin(email,password))
            if (response.isSuccessful) {
                val res = response.body()
                if (res != null) {
                    token = res.token
                    Log.d("TOKEN", "Logged in with token: $token")
                }
            }
        } catch (
            e: Exception
        ) {
            println("Exception: ${e.message}")
        }
    }
    suspend fun whoAmIm()  {
        try {
            val headers = mapOf(
                "Accept" to "application/json",
                "Content-Type" to "application/json",
                "Authorization" to "Bearer $token"
            )
            val response = userApiService.getId(headers)
            println(response)
            if(response.isSuccessful){
                val json = response.body()?:0
                userId =json
            }
        } catch (
            e: Exception
        ) {
            println("Exception: ${e.message}")
        }
    }



}