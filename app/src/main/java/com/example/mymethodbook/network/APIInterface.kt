package com.example.mymethodbook.network

import com.example.mymethodbook.model.TestResponse
import retrofit2.Call
import retrofit2.http.GET

interface APIInterface {
    @GET("/movie/readMovieList")
    fun test() : Call<TestResponse>
}