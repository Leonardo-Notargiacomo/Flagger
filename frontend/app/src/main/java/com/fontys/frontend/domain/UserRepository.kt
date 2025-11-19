package com.fontys.frontend.domain

import android.util.Log
import com.fontys.frontend.config.ApiConfig
import com.fontys.frontend.data.UserLogin
import com.fontys.frontend.data.UserRegister
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
import com.fontys.frontend.domain.UserAPIService



object UserRepository {
    private const val TAG = "UserRepository"
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
    private val userApiService = retrofit.create(UserAPIService::class.java)
    suspend fun getUser(userId: String): UserReturn? {
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
                val userData = response.body() ?: return null
                val id = userData.id
                val userName = userData.userName
                val email = userData.email
                val userImage = userData.userImage
                val bio = userData.bio
                return UserReturn(id, userName, email, userImage, bio)
            } else {
                // Handle error response
                Log.e(TAG, "Error getting user: ${response.code()} - ${response.message()}")
                return null
            }
        } catch (
            e: Exception
        ) {
            // Handle exceptions such as network errors
            Log.e(TAG, "Exception getting user: ${e.message}", e)
            return null
        }
    }
    suspend fun login(email : String, password: String) {
        try {
            val headers = HashMap<String, String>().apply {
                put("Accept", "application/json")
                put("Content-Type", "application/json")
            }
            val response = userApiService.login(headers, UserLogin(email,password))
            if(response.isSuccessful){
                val loginResponse = response.body()
                token = loginResponse?.token ?: ""
                Log.d(TAG, "Login successful, token set: ${token.take(20)}...")
            } else {
                Log.e(TAG, "Login failed: ${response.code()} - ${response.message()}")
            }
        } catch (
            e: Exception
        ) {
            Log.e(TAG, "Exception during login: ${e.message}", e)
        }
    }
    suspend fun whoAmIm()  {
        try {
            val headers = HashMap<String, String>().apply {
                put("Accept", "application/json")
                put("Content-Type", "application/json")
                token?.let { token ->
                    put("Authorization", "Bearer $token") // Add JWT token if available
                }
                    ?: run {
                        // Optional: Log a warning or throw an error if token is missing for authenticated endpoint
                        // throw IllegalStateException("JWT token is missing for authenticated request")
                    }
            }
            val response = userApiService.getId(headers)
            Log.d(TAG, "whoAmIm response: $response")
            if(response.isSuccessful){
                val json = response.body()?:0
                userId =json
            }
        } catch (
            e: Exception
        ) {
            Log.e(TAG, "Exception in whoAmIm: ${e.message}", e)
        }
    }

    suspend fun register(userName: String, email: String, password: String, bio: String): Boolean {
        val headers = HashMap<String, String>().apply {
            put("Accept", "application/json")
            put("Content-Type", "application/json")
        }
        val response =
            userApiService.signup(headers, UserRegister(userName, email, bio, password))
        if (response.isSuccessful) {
            // Signup successful, now login to get token
            login(email, password)
            whoAmIm()
            return true
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e(TAG, "Registration error: ${response.code()} - ${response.message()} - Body: $errorBody")

            // Check if it's a duplicate user error based on response
            val errorMessage = when {
                response.code() == 500 -> {
                    // Backend returns 500 for duplicate email constraints
                    "This email is already registered. Please use a different email or try logging in."
                }
                response.code() == 409 -> {
                    "This email is already registered. Please use a different email."
                }
                response.code() == 400 -> {
                    "Invalid registration data. Please check your information."
                }

                else -> {}
            }
            throw Exception(errorMessage as String?)
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