package com.fontys.frontend.domain

import com.fontys.frontend.data.AddFlagRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FlagApiService {
    @POST("flags")
    suspend fun addCords(@Body reqest : AddFlagRequest) : Response<String>
    @GET
    
}