package com.example.freebite2.model

import android.net.Uri

data class Chat(
    val messageId: String = "",
    val senderId: String = "",
    val sender: String = "",
    val profileImage: Uri? = null,
    val receiver: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0
    // Autres champs si nécessaire
)