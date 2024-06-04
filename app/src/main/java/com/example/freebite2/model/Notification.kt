package com.example.freebite2.model

data class Notification(
    var id: String = "",
    val senderId: String = "",
    val message: String = "",
    val timestamp: String = "",
    val type: String = "",
    val profileImageUrl: String = "",
    val OfferID: String? = null
)
