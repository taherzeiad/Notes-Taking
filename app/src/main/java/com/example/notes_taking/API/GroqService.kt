package com.example.notes_taking.API

import android.util.Log
import com.example.notes_taking.BuildConfig

object GroqService {
    private const val API_KEY = BuildConfig.GROQ_API_KEY
    suspend fun raseText(text: String): String {
        val request = ChatRequest(
            model = "llama-3.3-70b-versatile", messages = listOf(
                ChatMessage(
                    "system", "You are a helpful assistant. Return only the rephrased text."
                ), ChatMessage("user", "Rephrase this: $text")
            )
        )
        return makeRequest(request)
    }

    suspend fun diacritizeText(text: String): String {
        val request = ChatRequest(
            model = "llama-3.3-70b-versatile", messages = listOf(
                ChatMessage(
                    "system",
                    "أنت مساعد خبير في تشكيل النصوص العربية. قم بإعادة النص مع الحركات فقط."
                ), ChatMessage("user", "قم بتشكيل النص التالي: $text")
            )
        )
        return makeRequest(request)
    }

    private suspend fun makeRequest(request: ChatRequest): String {
        val authHeader = "Bearer $API_KEY"
        val response = RetrofitClient.instance.generateChatCompletion(authHeader, request)

        return if (response.isSuccessful) {
            response.body()?.choices?.firstOrNull()?.message?.content ?: "No response"
        } else {
            val error = response.errorBody()?.string()
            Log.e("GroqAPI", "Error: $error")
            throw Exception("API Error: ${response.code()}")
        }
    }
}