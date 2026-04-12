package com.example.notes_taking.API

// طلب OpenAI يتوقع موديل ورائمة رسائل
data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val role: String, // "system" أو "user"
    val content: String
)

// استجابة OpenAI
data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)
