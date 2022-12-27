package com.example.mymethodbook.network

import com.example.mymethodbook.model.TestResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

val authorization = "AAAA5PMPYTY:APA91bGtkFS2xi2wFV7Ils8hKDjwYI8n_tuuc-UCJpJghJJNsTaK70IJvv3c59-OYkoxAkowWrmn4x78dN_Emkj9czL3rZKZocISLu29SePkqb-GtgdDl0UolG6VL1Go5WW7z3XmpN7B"

interface APIInterface {
    @GET("movie/readMovieList")
    suspend fun test() : TestResponse

    /*@POST("send")
    suspend fun notifyToParents(
        @Header("Content-Type") ContentType : String = "application/json",
        @Header("Authorization") Authorization : String = "key=$authorization",
        @Body message : com.example.mymethodbook.model.Message
    )*/
}