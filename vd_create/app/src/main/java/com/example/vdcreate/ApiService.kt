package com.example.vdcreate

import retrofit2.Call
import retrofit2.http.*

data class Item(val id: String, val code: String, val battery: Int, val status: String)

interface ApiService {
    @POST("/items")
    fun addItem(@Body item: Item): Call<Item>

    @DELETE("/items/{id}")
    fun deleteItem(@Path("id") id: String): Call<Void>
}



//Retrofit 인터페이스 정의
