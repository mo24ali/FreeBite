package com.example.freebite2.model

import android.location.Location
data class User(
    val uid: String = "",
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val location: UserLocation = UserLocation()
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", UserLocation())
}

data class UserLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
/*data class User(
    val uid: String,
    val nom: String,
    val prenom: String,
    val email: String,
    val profilePictureUrl: String,
    val location: Location,
   // val isAdmin: Boolean = false

)*/

