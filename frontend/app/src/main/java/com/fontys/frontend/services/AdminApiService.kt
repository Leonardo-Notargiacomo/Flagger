package com.fontys.frontend.services


import com.fontys.frontend.data.FlagResponse
import com.fontys.frontend.data.UserReturn
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApiService {
    @GET("/go-users/{id}/is-admin")
    suspend fun isAdmin(@HeaderMap headers: Map<String, String>, @Path("id") userId: Int) : Response<Boolean>
    @GET("/flags")
    suspend fun getFlags(@HeaderMap headers: Map<String, String>, @Query("filter") filter: String) : Response<List<FlagResponse>>
    @GET("/go-users")
    suspend fun getUsers(@HeaderMap headers: Map<String, String>, @Query("filter") filter: String) : Response<List<UserReturn>>

    companion object
}
