package com.example.mymethodbook.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


object APIClient {
    /*val retrofit = Retrofit.Builder()
        .baseUrl("https://fcm.googleapis.com/fcm/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()*/
    val retrofit = Retrofit.Builder()
        .baseUrl("http://boostcourse-appapi.connect.or.kr:10000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiInterface : APIInterface = retrofit.create(APIInterface::class.java)
}