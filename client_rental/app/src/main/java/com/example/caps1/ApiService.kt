package com.example.caps1

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class Item(
        val id: String,
        val code: String,
        val battery: Int,
        val status: String
)

data class QrCodeRequest(val code: String)
data class QrCodeResponse(val message: String)

interface ApiService {
    @GET("/items")
    fun getItems(): Call<List<Item>>

    @POST("/generate_qr")
    fun generateQrCode(@Body request: QrCodeRequest): Call<QrCodeResponse>
}

