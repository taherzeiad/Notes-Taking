package com.example.myuniqueapp.API.Groq

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun generateChatCompletion(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://api.groq.com/openai/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)

        // ← الـ interceptor الجديد هنا
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            if (!response.isSuccessful) {
                val errorBody = response.peekBody(Long.MAX_VALUE).string()
                Log.e("GROQ_ERROR", "Code: ${response.code}")
                Log.e("GROQ_ERROR", "Body: $errorBody")
            }
            response
        }

        .addInterceptor { chain ->
            val request = chain.request()
            val auth = request.header("Authorization") ?: "NULL"
            Log.d("RETROFIT_DEBUG", "Auth length: ${auth.length}")
            Log.d("RETROFIT_DEBUG", "Starts with Bearer: ${auth.startsWith("Bearer ")}")
            Log.d("RETROFIT_DEBUG", "Preview: ${auth.take(15)}...")
            chain.proceed(request)
        }

        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val instance: OpenAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }
}