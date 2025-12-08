package com.fontys.frontend.services


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

interface AdminApiService {
    @GET("/go-users/{id}/is-admin")
    suspend fun isAdmin(@HeaderMap headers: Map<String, String>, @Path("id") userId: Int) : Response<Boolean>

    companion object
}