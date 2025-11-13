package com.fontys.frontend.services

import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.data.UserUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.HeaderMap


interface UserAPIService {
    @GET("go-users/{id}")
    suspend fun getUser(@HeaderMap headers:Map<String,String> ,@Path("id") userId: String): Response<UserReturn>

    @PATCH("go-users/{id}")
    suspend fun updateUser(@HeaderMap headers:Map<String,String> ,@Path("id") userId: String, @Body user: UserUpdate): Response<String>

    companion object

}