package com.example.freebite2.model

data class OffreModel(
    var offreName: String,
    var offreDescription: String,
    var offreProviderId: Long, // Changed to Long to match Firebase data type
    var offreDuration: String
)
