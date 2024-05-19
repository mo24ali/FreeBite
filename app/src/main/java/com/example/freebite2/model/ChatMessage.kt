package com.example.freebite2.model

data class ChatMessage(
    val messageID: String,
    val senderID: String,
    val receiverID: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
