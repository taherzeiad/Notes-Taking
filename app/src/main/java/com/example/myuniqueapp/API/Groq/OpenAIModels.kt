package com.example.myuniqueapp.API.Groq

import com.google.gson.annotations.SerializedName

// طلب OpenAI يتوقع موديل ورائمة رسائل
data class ChatRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<ChatMessage>,
)

data class ChatMessage(
    @SerializedName("role") val role: String, @SerializedName("content") val content: String
)

// استجابة OpenAI
data class ChatResponse(
    @SerializedName("choices") val choices: List<Choice>
)


data class Choice(
    @SerializedName("message") val message: ChatMessage
)

