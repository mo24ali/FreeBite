package com.example.freebite2.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.freebite2.R
import com.example.freebite2.databinding.ActivityAddOffreBinding
import com.example.freebite2.model.OffreModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddOffreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddOffreBinding
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val IMAGE_PICK_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_offre)
        database = FirebaseDatabase.getInstance().getReference("offres")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        storageReference = FirebaseStorage.getInstance().reference

        binding.btnSelectImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_PICK_REQUEST_CODE)
            } else {
                pickImage()
            }
        }

        binding.btnAddOffre.setOnClickListener {
            val offreName = binding.etOffreName.text.toString().trim()
            val offreDescription = binding.etOffreDescription.text.toString().trim()
            val offreDuration = binding.etOffreDuration.text.toString().trim()

            if (offreName.isNotEmpty() && offreDescription.isNotEmpty() && offreDuration.isNotEmpty() && imageUri != null) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                } else {
                    uploadImageAndSaveOffer(userId, offreDescription, offreDuration, offreName)
                }
            } else {
                showToast("Please fill in all fields and select an image")
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.imageView.setImageURI(imageUri)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == IMAGE_PICK_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val offreName = binding.etOffreName.text.toString().trim()
                val offreDescription = binding.etOffreDescription.text.toString().trim()
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val offreDuration = binding.etOffreDuration.text.toString().trim()
                uploadImageAndSaveOffer(userId, offreDescription, offreDuration, offreName)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageAndSaveOffer(userId: String, offreDescription: String, offreDuration: String, offreName: String) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val imageRef = storageReference.child("images/$fileName")

        imageUri?.let {
            imageRef.putFile(it).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    getLastLocation(userId, offreDescription, offreDuration, offreName, uri.toString())
                }
            }.addOnFailureListener {
                showToast("Failed to upload image")
            }
        }
    }

    private fun getLastLocation(userId: String, offreDescription: String, offreDuration: String, offreName: String, imageUrl: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission is not granted", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val offreId = database.push().key ?: ""
                val offre = OffreModel(
                    providerID = userId,
                    offerID = offreId,
                    nameoffre = offreName,
                    details = offreDescription,
                    duration = offreDuration,
                    latitude = latitude,
                    longitude = longitude,
                    pictureUrl = imageUrl
                )

                database.child(offreId).setValue(offre).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.etOffreName.text.clear()
                        binding.etOffreDescription.text.clear()
                        binding.etOffreDuration.text.clear()
                        binding.imageView.setImageResource(R.drawable.placeholder)
                        showToast("Offer added successfully")
                    } else {
                        showToast("Failed to add offer")
                    }
                }
            } else {
                Toast.makeText(this, "Unable to get location. Make sure location is enabled on the device.", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error obtaining location: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
