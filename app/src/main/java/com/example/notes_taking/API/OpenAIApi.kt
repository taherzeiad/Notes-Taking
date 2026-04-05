package com.example.notes_taking.API

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun generateChatCompletion(
        @Header("Authorization") token: String, // OpenAI يطلب المفتاح في الـ Header
        @Body request: ChatRequest
    ): retrofit2.Response<ChatResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://api.groq.com/openai/"

    private val client = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

    val instance: OpenAIApi by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(OpenAIApi::class.java)
    }
}