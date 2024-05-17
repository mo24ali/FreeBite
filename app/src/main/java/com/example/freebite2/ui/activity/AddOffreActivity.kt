package com.example.freebite2.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.freebite2.R
import com.example.freebite2.databinding.ActivityAddOffreBinding
import com.example.freebite2.model.OffreModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddOffreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddOffreBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_offre)
        database = FirebaseDatabase.getInstance().getReference("offres")

        binding.btnAddOffre.setOnClickListener {
            val offreName = binding.etOffreName.text.toString().trim()
            val offreDescription = binding.etOffreDescription.text.toString().trim()
            val offreDuration = binding.etOffreDuration.text.toString().trim()

            if (offreName.isNotEmpty() && offreDescription.isNotEmpty()  && offreDuration.isNotEmpty()) {
                val offreId = database.push().key ?: ""
                val offre = OffreModel(offreName, offreDescription, 22, offreDuration)
                database.child(offreId).setValue(offre).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.etOffreName.text.clear()
                        binding.etOffreDescription.text.clear()
                        binding.etOffreDuration.text.clear()
                        showToast("Offer added successfully")
                    } else {
                        showToast("Failed to add offer")
                    }
                }
            } else {
                showToast("Please fill in all fields")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
