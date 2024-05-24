@file:Suppress("DEPRECATION")

package com.example.freebite2.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.model.OffreModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class EditOffreFragment : Fragment(), OnMapReadyCallback {

    private lateinit var editTextNameOffre: EditText
    private lateinit var editTextDetailsOffre: EditText
    private lateinit var editTextDurationOffre: EditText
    private lateinit var imageViewOffer: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonSaveOffre: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var dialogueProgress: ProgressDialog

    private var offer: OffreModel? = null
    private var imageUri: Uri? = null
    private var imageUploadOffre: Uri? = null
    private lateinit var auth: FirebaseAuth

    private val requestCameraPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
            pickImageCamera()
        } else {
            Toast.makeText(requireContext(), "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pickImageGallery()
        } else {
            Toast.makeText(requireContext(), "Storage permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let { uploadImageToFirebase(it) }
        } else {
            Toast.makeText(requireContext(), "Camera capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            uploadImageToFirebase(uri)
        }
    }

    private val PERMISSIONS_REQUEST_CODE = 100
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            offer = it.getParcelable("offer")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_offre, container, false)

        editTextNameOffre = view.findViewById(R.id.editTextNameOffre)
        editTextDetailsOffre = view.findViewById(R.id.editTextDetailsOffre)
        editTextDurationOffre = view.findViewById(R.id.editTextDurationOffre)
        imageViewOffer = view.findViewById(R.id.imageViewOffer)
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage)
        buttonSaveOffre = view.findViewById(R.id.buttonSaveOffre)

        dialogueProgress = ProgressDialog(requireContext())
        dialogueProgress.setMessage("Uploading Image...")
        dialogueProgress.setCancelable(false)

        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (!hasPermissions()) {
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }



        offer?.let {
            editTextNameOffre.setText(it.nameoffre)
            editTextDetailsOffre.setText(it.details)
            editTextDurationOffre.setText(it.duration)
            Glide.with(this).load(it.pictureUrl).into(imageViewOffer)
        }

        buttonSelectImage.setOnClickListener {
            imagePickDialog()
        }

        buttonSaveOffre.setOnClickListener {
            saveOfferChanges()
        }

        return view
    }

    private fun hasPermissions(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun imagePickDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Image From")
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
        builder.show()
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        imageUri?.let { cameraActivityResultLauncher.launch(it) }
    }

    private fun pickImageGallery() {
        galleryActivityResultLauncher.launch("image/*")
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().getReference("offer_images/${UUID.randomUUID()}.jpg")

        dialogueProgress.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                storageReference.putFile(uri).await()
                val downloadUri = storageReference.downloadUrl.await()
                imageUploadOffre = downloadUri
                withContext(Dispatchers.Main) {
                    Glide.with(this@EditOffreFragment).load(imageUri).into(imageViewOffer)
                    dialogueProgress.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                    dialogueProgress.dismiss()
                }
            }
        }
    }

    private fun saveOfferChanges() {
        val userId = auth.currentUser?.uid ?: return

        val title = editTextNameOffre.text.toString().trim()
        val description = editTextDetailsOffre.text.toString().trim()
        val duration = editTextDurationOffre.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || duration.isEmpty() || imageUploadOffre == null) {
            Toast.makeText(requireContext(), "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val updatedOffer = offer?.copy(
                            nameoffre = title,
                            details = description,
                            duration = duration,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            pictureUrl = imageUploadOffre.toString()
                        )

                        val offerRef = FirebaseDatabase.getInstance().getReference("offres").child(offer?.offerID ?: return@addOnSuccessListener)
                        offerRef.setValue(updatedOffer)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Offer updated successfully", Toast.LENGTH_SHORT).show()
                                activity?.supportFragmentManager?.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to update offer: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Permissions required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        googleMap.addMarker(MarkerOptions().position(latLng).title("Votre offre"))
                    }
                }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(offer: OffreModel) = EditOffreFragment().apply {
            arguments = Bundle().apply {
                putParcelable("offer", offer)
            }
        }
    }
}
