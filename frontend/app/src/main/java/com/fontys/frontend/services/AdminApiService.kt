package com.fontys.frontend.services

import com.fontys.frontend.data.models.AdminResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

interface AdminApiService {
    @GET("/go-users/{id}/is-admin")
    suspend fun getCords(@HeaderMap headers: Map<String, String>, @Path("userId") userId: Int) : Response<List<AdminResponse>>

    companion object
}