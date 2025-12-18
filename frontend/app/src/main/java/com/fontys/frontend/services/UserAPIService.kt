package com.fontys.frontend.services

import com.fontys.frontend.data.CustomFlagUpdate
import com.fontys.frontend.data.LoginResponse
import com.fontys.frontend.data.UserLogin
import com.fontys.frontend.data.UserRegister
import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.data.UserUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserAPIService {
    @GET("go-users/{id}")
    suspend fun getUser(@HeaderMap headers:Map<String,String>, @Path("id") userId: String): Response<UserReturn>

    @PATCH("go-users/{id}")
    suspend fun updateUser(@HeaderMap headers:Map<String,String>, @Path("id") userId: String, @Body user: UserUpdate): Response<String>
    @POST("login")
    suspend fun login(@HeaderMap headers: Map<String,String>, @Body user: UserLogin) : Response<LoginResponse>
    @GET("whoAmI")
    suspend fun getId(@HeaderMap headers: Map<String,String>): Response<Int>
    @POST(value = "signup")
    suspend fun signup(@HeaderMap headers: Map<String,String>,@Body user: UserRegister ): Response<UserReturn>

    @PATCH("user-custom-flags")
    suspend fun customFlagUpdate(@HeaderMap headers: Map<String,String>,@Body customFlagUpdate: CustomFlagUpdate)
    @GET ("user-custom-flags/{goUserId}")
    suspend fun userFlagStyle(@HeaderMap headers: Map<String,String>,@Path("id") userId: String)
}