package com.fontys.frontend.services

import com.fontys.frontend.data.AddFlagRequest
import com.fontys.frontend.data.CustomFlagUpdate
import com.fontys.frontend.data.FlagResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface FlagApiService {


    @POST("flags")
    suspend fun addCords(@HeaderMap headers: Map<String, String>, @Body request : AddFlagRequest) : Response<String>
    @GET("/flags/user/{userId}")
    suspend fun getCords(@HeaderMap headers: Map<String, String>,  @Path("userId") userId: Int) : Response<List<FlagResponse>>

    @PATCH("user-custom-flags")
    suspend fun customFlagUpdate(@HeaderMap headers: Map<String,String>,@Body customFlagUpdate: CustomFlagUpdate): Response<String>
    @GET ("user-custom-flag/{goUserId}")
    suspend fun userFlagStyle(@HeaderMap headers: Map<String,String>,@Path("goUserId") userId: Int) : Response<CustomFlagUpdate>
}