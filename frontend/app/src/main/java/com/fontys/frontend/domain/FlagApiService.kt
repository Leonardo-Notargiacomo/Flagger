package com.fontys.frontend.domain

import com.fontys.frontend.data.AddFlagRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST

interface FlagApiService {


    @POST("flags")
    suspend fun addCords(@HeaderMap headers: Map<String, String>, @Body request : AddFlagRequest) : Response<String>
    @GET("/flags/user/{userId}")
    suspend fun getCords(@Body userId : Int) : Response<String>

    companion object

}