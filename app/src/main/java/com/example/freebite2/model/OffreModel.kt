package com.example.freebite2.model

data class OffreModel(
    var userID: String? = null,
    var offerID: String? = null,
    var details: String? = null,
    var distance: Double? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var imageUrls: List<String>? = null,
    var timestamp: Long = System.currentTimeMillis(),
    var status: String = "available" // values can be "available", "reserved", "completed"

) {
    constructor() : this(null, null, null, null, null, null)
}
