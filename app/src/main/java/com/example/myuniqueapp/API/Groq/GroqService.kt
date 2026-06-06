package com.example.myuniqueapp.API.Groq

import android.util.Log
import com.example.myuniqueapp.API.Groq.ChatMessage
import com.example.myuniqueapp.API.Groq.ChatRequest
import com.example.myuniqueapp.API.Groq.RetrofitClient
import com.example.myuniqueapp.BuildConfig
import java.util.Locale

object GroqService {
    private const val API_KEY = BuildConfig.GROQ_API_KEY

    suspend fun rephraseText(text: String): String {
        val langInstruction = getResponseLanguageInstruction()
        val request = ChatRequest(
            model = "llama-3.3-70b-versatile", messages = listOf(
                ChatMessage(
                    "system",
                    "You are a helpful assistant. Return only the rephrased text. $langInstruction"
                ), ChatMessage("user", "Rephrase this: $text")
            )
        )
        return makeRequest(request)
    }

    suspend fun diacritizeText(text: String): String {
        // هذه دائماً عربية لأن التشكيل خاص بالعربية
        val request = ChatRequest(
            model = "llama-3.3-70b-versatile", messages = listOf(
                ChatMessage(
                    "system",
                    "أنت مساعد خبير في تشكيل النصوص العربية. قم بإعادة النص مع الحركات فقط. يجب أن يكون ردك باللغة العربية فقط."
                ), ChatMessage("user", "قم بتشكيل النص التالي: $text")
            )
        )
        return makeRequest(request)
    }

    suspend fun classifyNoteContent(text: String): String {
        // التصنيف دائماً بالإنجليزية لأنها قيم برمجية
        val request = ChatRequest(
            model = "llama-3.3-70b-versatile", messages = listOf(
                ChatMessage(
                    "system",
                    "You are a professional organizer. Categorize the user's note into one of these: [Philosophy, Literature, Self-Development, Personal, Work, Task]. Return only the category name in English."
                ), ChatMessage("user", "Categorize this: $text")
            )
        )
        return try {
            makeRequest(request).trim()
        } catch (e: Exception) {
            "General"
        }
    }

    suspend fun extractTasksFromNote(noteTitle: String, noteContent: String): List<String> {

        val isArabic = Locale.getDefault().language == "ar"

        val request = ChatRequest(
            model = "llama-3.3-70b-versatile", messages = listOf(
                ChatMessage(
                    "system", if (isArabic) {
                        """أنت مساعد متخصص في استخراج المهام من الملاحظات.

استخرج المهام القابلة للتنفيذ فقط.

مهم جداً:
انسخ المهمة كما هي حرفياً من النص الأصلي.
لا تترجم.
لا تعيد الصياغة.
لا تختصر.
لا تعدل أي كلمة.
حافظ على النص الأصلي كما هو تماماً.

كل مهمة في سطر منفصل.

إذا لم توجد مهام واضحة أعد NONE فقط.
"""
                    } else {
                        """You are an assistant specialized in extracting tasks from notes.

Extract only actionable tasks from the text.

IMPORTANT:
Copy tasks exactly from the original note.
Do not translate.
Do not rephrase.
Do not summarize.
Do not modify any words.
Preserve the original wording exactly.

Return one task per line.

If no clear tasks are found, return NONE only.
"""
                    }
                ), ChatMessage(
                    "user",
                    if (isArabic) "استخرج المهام من هذه الملاحظة:\nالعنوان: $noteTitle\nالمحتوى: $noteContent"
                    else "Extract tasks from this note:\nTitle: $noteTitle\nContent: $noteContent"
                )
            )
        )
        return try {
            val result = makeRequest(request).trim()
            if (result.uppercase() == "NONE" || result.isBlank()) {
                emptyList()
            } else {
                result.lines().map { it.trim() }
                    .filter { it.isNotBlank() && it.uppercase() != "NONE" }
            }
        } catch (e: Exception) {
            Log.e("GroqService", "extractTasks failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun summarizeNotes(notes: List<String>, targetDate: String): String {
        val langInstruction = getResponseLanguageInstruction()
        val isArabic = Locale.getDefault().language == "ar"

        val notesText = notes.mapIndexed { i, note -> "${i + 1}. $note" }.joinToString("\n")

        val request = ChatRequest(
            model = "meta-llama/llama-4-scout-17b-16e-instruct", messages = listOf(
                ChatMessage(
                    "system", if (isArabic) {
                        """أنت مساعد ذكي متخصص في تلخيص الملاحظات اليومية.
مهمتك: تلخيص ملاحظات المستخدم ليوم $targetDate بشكل واضح ومنظم.
اكتب الملخص في النقاط التالية:
1. 📝 أبرز الأفكار
2. ✅ المهام المذكورة
3. 💡 التوصيات
اجعل الملخص مختصراً ومفيداً.
$langInstruction"""
                    } else {
                        """You are a smart assistant specialized in summarizing daily notes.
Your task: Summarize the user's notes for $targetDate in a clear and organized way.
Write the summary in these points:
1. 📝 Key Ideas
2. ✅ Mentioned Tasks
3. 💡 Recommendations
Keep the summary concise and useful.
$langInstruction"""
                    }
                ), ChatMessage(
                    "user", if (isArabic) "لخص هذه الملاحظات:\n$notesText"
                    else "Summarize these notes:\n$notesText"
                )
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

    private fun getResponseLanguageInstruction(): String {
        return if (Locale.getDefault().language == "ar") {
            "يجب أن يكون ردك باللغة العربية فقط."
        } else {
            "Your response must be in English only."
        }
    }
}