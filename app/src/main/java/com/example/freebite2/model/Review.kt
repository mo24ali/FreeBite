package com.example.freebite2.model

data class Review(
    val reviewID: String,
    val reviewerID: String,
    val revieweeID: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
