package com.example.freebite2.model

import android.location.Location

data class User(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePictureUrl: String? = "app/src/main/res/drawable/profileiamge.png",
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val location: Location
)
