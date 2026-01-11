package com.example.vdcreate

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://IP:PORT" // 실행 시 환경에 맞게 IP와 PORT를 수정하여 사용

    val apiService: ApiService by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
    }
}

//Retrofit 설정

