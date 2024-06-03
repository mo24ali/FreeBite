@file:Suppress("DEPRECATION")

package com.example.freebite2.ui.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.ActivityEditOfferBinding
import com.example.freebite2.model.OffreModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class EditOfferActivity : AppCompatActivity() {

    private lateinit var location: Location
    private lateinit var dialogueProgress: ProgressDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var uploadedImageView: ImageView
    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var dureeModifEditText: TextInputEditText
    private lateinit var mapView: SupportMapFragment
    private lateinit var btnSubmit: MaterialButton
    private lateinit var binding: ActivityEditOfferBinding
    private var imageUploadOffre: Uri? = null
    private var offer: OffreModel? = null

    private val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUploadOffre?.let { uploadImageToFirebase(it) }
        } else {
            Toast.makeText(this, "Échec de la capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUploadOffre = uri
            uploadImageToFirebase(uri)
        }
    }

    private val requestCameraPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
            pickImageCamera()
        } else {
            Toast.makeText(this, "Les permissions de la caméra et du stockage sont nécessaires", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pickImageGallery()
        } else {
            Toast.makeText(this, "La permission de stockage est nécessaire", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialogueProgress = ProgressDialog(this)
        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        uploadedImageView = binding.uploadedImageView
        titleEditText = binding.titleEditText
        descriptionEditText = binding.descriptionEditText
        dureeModifEditText = binding.DureeModifEditText
        mapView = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        btnSubmit = binding.btnsubmit

        binding.modifyOfferPic.setOnClickListener {
            chooseImage()
        }
        uploadedImageView.setOnClickListener {
            chooseImage()
        }

        findViewById<ImageButton>(R.id.btnbackhome).setOnClickListener {
            finish()
        }

        offer = intent.getParcelableExtra("offer")
        offer?.let {
            titleEditText.setText(it.nameoffre)
            descriptionEditText.setText(it.details)
            dureeModifEditText.setText(it.duration)
            if (!it.pictureUrl.isNullOrEmpty()) {
                imageUploadOffre = Uri.parse(it.pictureUrl)
                Glide.with(this)
                    .load(imageUploadOffre)
                    .into(binding.uploadedImageView)
            }
        }

        btnSubmit.setOnClickListener {
            saveOfferChanges()
        }
    }

    private fun chooseImage() {
        val options = arrayOf("Appareil photo", "Galerie")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choisir à partir de")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    Log.d(ContentValues.TAG, "imagePickDialog: Camera Clicked")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageCamera()
                    } else {
                        requestCameraPermissions.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                    }
                }
                1 -> {
                    Log.d(ContentValues.TAG, "imagePickDialog: Gallery Clicked")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery()
                    } else {
                        requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
        }
        builder.setOnCancelListener {
            binding.modifyOfferPic.isEnabled = true
        }
        builder.show()
    }

    private fun pickImageGallery() {
        galleryActivityResultLauncher.launch("image/*")
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Nouvelle image")
        values.put(MediaStore.Images.Media.DESCRIPTION, "De votre appareil photo")
        imageUploadOffre = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        imageUploadOffre?.let { cameraActivityResultLauncher.launch(it) }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().getReference("offre_images/${UUID.randomUUID()}.jpg")

        dialogueProgress.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                storageReference.putFile(uri).await()
                val downloadUri = storageReference.downloadUrl.await()
                imageUploadOffre = downloadUri
                withContext(Dispatchers.Main) {
                    Glide.with(this@EditOfferActivity).load(imageUploadOffre).into(binding.uploadedImageView)
                    dialogueProgress.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditOfferActivity, "Échec du téléchargement de l'image : ${e.message}", Toast.LENGTH_SHORT).show()
                    dialogueProgress.dismiss()
                }
            }
        }
    }

    private fun saveOfferChanges() {
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val duration = dureeModifEditText.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || duration.isEmpty() || imageUploadOffre == null) {
            Toast.makeText(this, "Veuillez remplir tous les champs et sélectionner une image", Toast.LENGTH_SHORT).show()
            return
        }

        offer?.let {
            val offerRef = FirebaseDatabase.getInstance().getReference("offres").child(it.offerID ?: return@let)
            val updatedOffer = OffreModel(
                it.offerID,
                title,
                description,
                duration,
                location.latitude,
                location.longitude,
                imageUploadOffre.toString()
            )

            offerRef.setValue(updatedOffer)
                .addOnSuccessListener {
                    Toast.makeText(this, "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show()
                    if (!it.pictureUrl.isNullOrEmpty() && it.pictureUrl != updatedOffer.pictureUrl) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(it.pictureUrl).delete()
                    }
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Échec de la mise à jour de l'offre : ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        offer?.let {
                            val offerRef = FirebaseDatabase.getInstance().getReference("offres").child(it.offerID ?: return@addOnSuccessListener)
                            val updatedOffer = OffreModel(
                                it.offerID,
                                title,
                                description,
                                duration,
                                location.latitude.toString(),
                                location.longitude,
                                imageUploadOffre.toString()
                            )

                            offerRef.setValue(updatedOffer)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show()
                                    if (!it.pictureUrl.isNullOrEmpty() && it.pictureUrl != updatedOffer.pictureUrl) {
                                        FirebaseStorage.getInstance().getReferenceFromUrl(it.pictureUrl).delete()
                                    }
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Échec de la mise à jour de l'offre : ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Impossible d'obtenir la localisation", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Échec de l'obtention de la localisation : ${e.message}", Toast.LENGTH_SHORT).
                    show()
                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveOfferChanges()
                } else {
                    Toast.makeText(this, "La permission de localisation est nécessaire", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}