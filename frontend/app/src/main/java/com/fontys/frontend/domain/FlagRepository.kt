package com.fontys.frontend.domain

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class FlagRepository{

    val BASE_URL = ""

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

}