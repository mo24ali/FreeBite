package com.example.freebite2.model

import android.location.Location

data class User(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePictureUrl: String,
    val location: Location
)
