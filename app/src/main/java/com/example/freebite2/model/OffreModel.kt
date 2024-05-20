package com.example.freebite2.model

data class OffreModel(
    var providerID: String? = null,
    var offerID: String? = null,
    var nameoffre: String? = null,
    var details: String? = null,
    var duration: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var pictureUrl: String? = null,
    var provider: User? = null,

) {
    constructor() : this(null, null, null, null, null, null,null, null)
}
