package com.example.mymethodbook.network

import com.example.mymethodbook.model.TestResponse
import retrofit2.Call
import retrofit2.http.GET

interface MyMethodService {
    @GET("/movie/readMovieList")
    fun testApi() : Call<TestResponse>
}