package com.example.myuniqueapp.API.Gemini

import android.util.Log
import com.example.myuniqueapp.BuildConfig
import java.util.Locale

object GeminiService {

    private const val API_KEY = BuildConfig.GEMINI_API_KEY

    // ─── summarizeNotes (نفس signature بتاع Groq) ────────────────────────
    suspend fun summarizeNotes(notes: List<String>, targetDate: String): String {
        val isArabic = Locale.getDefault().language == "ar"
        val notesText = notes.mapIndexed { i, note -> "${i + 1}. $note" }.joinToString("\n")

        val prompt = if (isArabic) {
            """أنت مساعد ذكي متخصص في تلخيص الملاحظات اليومية.
لخص ملاحظات المستخدم ليوم $targetDate بشكل واضح ومنظم في النقاط التالية:
1. 📝 أبرز الأفكار
2. ✅ المهام المذكورة  
3. 💡 التوصيات
اجعل الملخص مختصراً ومفيداً.

الملاحظات:
$notesText

يجب أن يكون ردك باللغة العربية فقط."""
        } else {
            """You are a smart assistant specialized in summarizing daily notes.
Summarize the user's notes for $targetDate clearly and organized:
1. 📝 Key Ideas
2. ✅ Mentioned Tasks
3. 💡 Recommendations
Keep the summary concise and useful.

Notes:
$notesText

Your response must be in English only."""
        }

        return makeRequest(prompt)
    }

    // ─── Private helper ───────────────────────────────────────────────────
    private suspend fun makeRequest(prompt: String): String {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            )
        )

        val response = GeminiRetrofitClient.instance.generateContent(
            apiKey = API_KEY, request = request
        )

        return if (response.isSuccessful) {
            response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response"
        } else {
            val error = response.errorBody()?.string()
            Log.e("GeminiAPI", "Error: $error")
            throw Exception("Gemini API Error: ${response.code()}")
        }
    }
}