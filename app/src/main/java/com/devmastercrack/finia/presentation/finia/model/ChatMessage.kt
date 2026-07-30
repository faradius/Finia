package com.devmastercrack.finia.presentation.finia.model

enum class ChatSender { USER, AI }

enum class ChatMessageType { TEXT, SUMMARY, PAYMENTS, TIP, ANALYSIS }

data class ChatMessage(
    val from: ChatSender,
    val type: ChatMessageType,
    val time: String,
    val text: String? = null,
)
