package com.example.freebite2.ui.activity.admin

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.freebite2.databinding.ActivityEditPostBinding
import com.example.freebite2.model.OffreModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditPostActivity : AppCompatActivity() {

    private lateinit var offre: OffreModel
    private lateinit var binding: ActivityEditPostBinding
    private val adminUid = "tZtTbchwFPOse0jhTXt39kCnNGo1"

    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var durationEditText: TextInputEditText
    private lateinit var offerPicDetails: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        offerPicDetails = binding.uploadedImageView
        titleEditText = binding.titleEditText
        descriptionEditText = binding.descriptionEditText
        durationEditText = binding.durationEditText

        offre = intent.getParcelableExtra("offre") ?: throw IllegalArgumentException("Argument 'offre' must not be null")
        initializeViews()

        Glide.with(this)
            .load(offre.pictureUrl)
            .into(offerPicDetails)

        binding.btnsubmit.setOnClickListener {
            val updatedOffre = OffreModel(
                offre.providerID,
                offre.offerID,
                titleEditText.text.toString(),
                descriptionEditText.text.toString(),
                durationEditText.text.toString(),
                offre.latitude,
                offre.longitude,
                offre.pictureUrl
            )

            updateOffre(updatedOffre)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.uid != adminUid) {
            Toast.makeText(this, "Vous n'êtes pas autorisé à modifier ce post", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeViews() {
        titleEditText.setText(offre.nameoffre)
        descriptionEditText.setText(offre.details)
        durationEditText.setText(offre.duration)
    }

    private fun updateOffre(offre: OffreModel) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("offres").child(offre.offerID.toString()).setValue(offre)
            .addOnSuccessListener {
                Log.e("ManageEditDeletePostActivity", "Offre mise à jour avec succès")
                Toast.makeText(this, "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e("ManageEditDeletePostActivity", "Échec de la mise à jour de l'offre")
                Toast.makeText(this, "Échec de la mise à jour de l'offre", Toast.LENGTH_SHORT).show()
            }
    }
}
