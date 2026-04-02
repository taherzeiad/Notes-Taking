package com.example.notes_taking.API

import com.google.ai.client.generativeai.GenerativeModel

object GeminiService {
    private const val API_KEY = "APIKEY"
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    suspend fun rephraseText(text: String): String {
        return try {
            val prompt = "Rephrase the following text in a clear and natural way, keep the same language:\n\n$text"
            val response = model.generateContent(prompt)
            response.text ?: throw Exception("Gemini returned an empty response")
        } catch (e: Exception) {
            android.util.Log.e("GeminiService", "Error in rephraseText", e)
            throw e // إعادة إرسال الخطأ للشاشة لتظهره للمستخدم
        }
    }
    suspend fun diacritizeText(text: String): String {
        val prompt = "Add Arabic diacritics (tashkeel) to the following Arabic text:\n\n$text"
        val response = model.generateContent(prompt)
        return response.text ?: text
    }
}