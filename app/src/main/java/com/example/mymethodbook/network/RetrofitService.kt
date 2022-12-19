package com.example.mymethodbook.network

import com.example.mymethodbook.model.Movie
import retrofit2.Call
import retrofit2.http.GET

interface MyMethodBookService {
    @GET("/movie/readMovieList")
    fun readMovieList() : Call<List<Movie>>
}