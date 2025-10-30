package com.fontys.frontend.domain

import com.fontys.frontend.data.AddFlagRequest
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.POST


class FlagRepository{

    val BASE_URL = "https://group-repository-2025-android-1.onrender.com/"

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()
    private val flagApiService = retrofit.create(FlagApiService::class.java)

    suspend fun addFlag(userId: Int, placeId: String): Result<String> {
        return try {
            val requestBody = AddFlagRequest(userId, placeId)
            val response = flagApiService.addCords(requestBody)

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
            val response = flagApiService.getCords(userId)

            if(response.isSuccessful){

                return listOf()
            }
        } catch (e: Exception) {

        }
        return TODO("Provide the return value")
    }

}