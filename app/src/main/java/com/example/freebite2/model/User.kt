package com.example.freebite2.model

import android.location.Location

data class User(
    val uid: String,
    val nom: String,
    val prenom: String,
    val email: String,
    val profilePictureUrl: String,
    val location: Location,
    val isAdmin: Boolean = false

)
