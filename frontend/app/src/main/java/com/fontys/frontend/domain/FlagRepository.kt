package com.fontys.frontend.domain

import android.util.Log
import com.fontys.frontend.data.AddFlagRequest
import com.fontys.frontend.data.PlaceService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.POST
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.Date
import com.fontys.frontend.data.FlagResponse // Import your new FlagResponse data class
import kotlinx.serialization.builtins.UIntArraySerializer


class FlagRepository{

    val BASE_URL = "https://group-repository-2025-android-1.onrender.com/"
    var token = UserRepository.token
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
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    private val flagApiService = retrofit.create(FlagApiService::class.java)
    suspend fun addFlag(userId: Int, placeId: String, photoCode :String): Result<String> {
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
            val requestBody = AddFlagRequest(
                placeId, Date(), 0,
                userId,
                photoCode
            )
            val response = flagApiService.addCords(headers,requestBody)

            if (response.isSuccessful) {

                return Result.success(response.body() ?: "Flag added successfully")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                return Result.failure(Exception("HTTP error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getFlags(userId: Int) : List<String>{
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
            val response = flagApiService.getCords(headers, userId)

            if (response.isSuccessful) {
                val list = mutableListOf<String>()
                val flags = response.body() ?: listOf<FlagResponse>()
                println(flags.toString())
                val locationIds = flags.map { it.locationId }
                return locationIds
            } else{

            }
        } catch (e: Exception) {
            Log.e("FlagRepository", "Error parsing response", e)
        }
        return listOf()
    }
    suspend fun getFullFlags(userId: Int) : List<FlagResponse>{
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
            val response = flagApiService.getCords(headers, userId)

            if (response.isSuccessful) {
                val list = mutableListOf<String>()
                val flags = response.body() ?: listOf<FlagResponse>()
                println(flags.toString())
                return flags
            } else{

            }
        } catch (e: Exception) {
            Log.e("FlagRepository", "Error parsing response", e)
        }
        return listOf()
    }

}