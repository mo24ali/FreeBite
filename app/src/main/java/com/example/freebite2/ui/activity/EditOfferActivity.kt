@file:Suppress("DEPRECATION")

package com.example.freebite2.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.model.OffreModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class EditOfferActivity : AppCompatActivity(), OnMapReadyCallback {

    private val PICK_IMAGE_REQUEST = 1

    private lateinit var database: DatabaseReference
    private lateinit var offerID: String
    private var offer: OffreModel? = null

    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var dureeModifEditText: TextInputEditText
    private lateinit var uploadedImageView: ImageButton
    private lateinit var saveButton: MaterialButton
    private lateinit var mapView: SupportMapFragment
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_offer)

        // Initialisation des vues
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        dureeModifEditText = findViewById(R.id.DureeModifEditText)
        uploadedImageView = findViewById(R.id.uploadedImageView)
        saveButton = findViewById(R.id.btnsubmit)
        mapView = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapView.getMapAsync(this)

        // Initialisation de Firebase
        database = FirebaseDatabase.getInstance().getReference("offres")

        // Récupérer l'ID de l'offre passée en argument
        offerID = intent.getStringExtra("offerID") ?: ""

        // Charger les données de l'offre
        loadOfferData()

        // Sauvegarder les modifications
        saveButton.setOnClickListener {
            updateOffer()
        }

        // Choisir une image
        uploadedImageView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Sélectionnez une image"), PICK_IMAGE_REQUEST)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapClickListener { latLng ->
            latitude = latLng.latitude
            longitude = latLng.longitude
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Position de l'offre"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
        }
    }

    private fun loadOfferData() {
        database.child(offerID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    offer = snapshot.getValue(OffreModel::class.java)
                    if (offer != null) {
                        // Afficher les données de l'offre
                        titleEditText.setText(offer?.nameoffre)
                        descriptionEditText.setText(offer?.details)
                        dureeModifEditText.setText(offer?.duration)
                        latitude = offer?.latitude
                        longitude = offer?.longitude

                        // Charger l'image (utilisez une bibliothèque comme Glide)
                        Glide.with(this@EditOfferActivity)
                            .load(offer?.pictureUrl)
                            .into(uploadedImageView)

                        // Positionner la carte
                        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
                        mapFragment.getMapAsync { googleMap ->
                            val position = LatLng(offer?.latitude ?: 0.0, offer?.longitude ?: 0.0)
                            googleMap.addMarker(MarkerOptions().position(position).title("Position de l'offre"))
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12f))
                        }
                    } else {
                        Toast.makeText(this@EditOfferActivity, "Erreur : l'offre est nulle", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@EditOfferActivity, "Erreur de conversion des données", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditOfferActivity, "Erreur de chargement des données", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateOffer() {
        val name = titleEditText.text.toString()
        val details = descriptionEditText.text.toString()
        val duration = dureeModifEditText.text.toString()

        if (offer != null) {
            // Mettre à jour l'objet OffreModel
            offer?.nameoffre = name
            offer?.details = details
            offer?.duration = duration
            offer?.latitude = latitude
            offer?.longitude = longitude

            // Mettre à jour l'URL de l'image si une nouvelle image a été sélectionnée
            if (imageUri != null) {
                uploadImageToFirebase(imageUri!!) { imageUrl ->
                    offer?.pictureUrl = imageUrl
                    saveOfferToFirebase()
                }
            } else {
                saveOfferToFirebase()
            }
        } else {
            Toast.makeText(this, "Erreur : l'offre est nulle", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveOfferToFirebase() {
        database.child(offerID).setValue(offer).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@EditOfferActivity, "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@EditOfferActivity, "Erreur de mise à jour", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri, callback: (String) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference.child("offres/${UUID.randomUUID()}.jpg")
        storageReference.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Échec du téléchargement de l'image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(uploadedImageView)
        }
    }
}


