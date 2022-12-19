package com.example.mymethodbook.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object APIClient {
    var retrofit = Retrofit.Builder()
        .baseUrl("http://boostcourse-appapi.connect.or.kr:10000")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var service: MyMethodService = retrofit.create(MyMethodService::class.java)
}