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
                val errorMessage = when(response.code()) {
                    401 -> "Invalid email or password"
                    404 -> "User not found"
                    500 -> "Server error, please try again later"
                    else -> "Login failed: ${response.message()}"
                }
                Log.e(TAG, "Login failed: ${response.code()} - ${response.message()}")
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during login: ${e.message}", e)
            throw e
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

            // Try to parse error message from backend
            val errorMessage = try {
                if (!errorBody.isNullOrBlank()) {
                    // First, try to clean up the JSON if it has single quotes instead of double quotes
                    val cleanedBody = errorBody.replace("'", "\"")
                    val json = JSONObject(cleanedBody)

                    // Try to extract the error message from various possible fields
                    val msg = when {
                        json.has("error") && json.getJSONObject("error").has("message") -> {
                            json.getJSONObject("error").getString("message")
                        }
                        json.has("message") -> json.getString("message")
                        json.has("error") -> json.getString("error")
                        json.has("detail") -> json.getString("detail")
                        else -> null
                    }

                    // Log the extracted message for debugging
                    Log.d(TAG, "Extracted error message: $msg")
                    msg
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse error body: ${e.message}. Raw body: $errorBody")
                // Try regex as fallback to extract message from malformed JSON
                try {
                    val messagePattern = """['"']message['"']\s*:\s*['"']([^'"']+)['"']""".toRegex()
                    val match = messagePattern.find(errorBody ?: "")
                    match?.groupValues?.getOrNull(1)
                } catch (regexError: Exception) {
                    Log.e(TAG, "Regex extraction also failed: ${regexError.message}")
                    null
                }
            }

            // Use backend error message if available, otherwise use generic messages based on status code
            val finalErrorMessage = when {
                // Prioritize backend error message if it's specific and not generic
                !errorMessage.isNullOrBlank() && errorMessage != "Internal Server Error" -> {
                    errorMessage
                }
                // Fallback to status code-based messages
                response.code() == 409 -> {
                    "This username or email is already registered."
                }
                response.code() == 500 -> {
                    "This username or email is already taken. Please try a different one."
                }
                response.code() == 400 -> {
                    "Invalid registration data. Please check your information."
                }
                else -> {
                    "Registration failed. Please try again."
                }
            }

            Log.d(TAG, "Final error message to display: $finalErrorMessage")
            throw Exception(finalErrorMessage)
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