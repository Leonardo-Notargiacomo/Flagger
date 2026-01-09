package com.fontys.frontend.domain

import com.fontys.frontend.data.LoginResponse
import com.fontys.frontend.data.UserLogin
import com.fontys.frontend.data.UserRegister
import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.data.UserUpdate
import com.fontys.frontend.data.models.ChangePasswordRequest
import com.fontys.frontend.data.models.Flag
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path

interface UserAPIService {
    @GET("go-users/{id}")
    suspend fun getUser(@HeaderMap headers:Map<String,String>, @Path("id") userId: String): Response<UserReturn>

    @PATCH("go-users/{id}")
    suspend fun updateUser(@HeaderMap headers:Map<String,String>, @Path("id") userId: String, @Body user: UserUpdate): Response<String>
    @PATCH("/go-users/me/password")
    suspend fun updatePassword(
        @HeaderMap headers:Map<String,String>,
        @Body passwords: ChangePasswordRequest
    ): Response<String>
    @POST("login")
    suspend fun login(@HeaderMap headers: Map<String,String>, @Body user: UserLogin) : Response<LoginResponse>
    @GET("whoAmI")
    suspend fun getId(@HeaderMap headers: Map<String,String>): Response<Int>
    @POST(value = "signup")
    suspend fun signup(@HeaderMap headers: Map<String,String>,@Body user: UserRegister ): Response<UserReturn>

    @GET("/flags/user/{userID}")
    suspend fun getUserFlags(
        @HeaderMap headers: Map<String, String>,
        @Path("userID") userID: String
    ): Response<List<Flag>>

    @DELETE("go-users/{id}")
    suspend fun deleteUser(
        @HeaderMap headers: Map<String, String>,
        @Path("id") userId: String
    ): Response<Unit>
}
