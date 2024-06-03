package com.example.freebite2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.freebite2.model.OffreModel
import com.example.freebite2.model.User
import com.google.firebase.firestore.FirebaseFirestore

class OffreViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun fetchProviderDetails(offre: OffreModel, onComplete: (User?) -> Unit) {
        val providerID = offre.providerID ?: return onComplete(null)

        db.collection("Users").document(providerID).get().addOnSuccessListener { document ->
            val user = document.toObject(User::class.java)
            //offre.provider = user
            onComplete(user)
        }.addOnFailureListener {
            onComplete(null)
        }
    }
}
