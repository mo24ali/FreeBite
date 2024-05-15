package com.example.freebite2.model

import android.location.Location

data class User(
    val uid: String,
    val name: String,
    val location: Location
)