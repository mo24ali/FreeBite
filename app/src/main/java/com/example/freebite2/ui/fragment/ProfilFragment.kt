package com.example.freebite2.ui.fragment

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.freebite2.databinding.FragmentProfilBinding
import com.example.freebite2.ui.activity.MainActivity
import com.example.freebite2.ui.activity.UserOffersActivity
import com.example.freebite2.util.SharedPreferencesUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


@Suppress("DEPRECATION")
class ProfilFragment : Fragment() {

    private lateinit var binding: FragmentProfilBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialogueProgress: ProgressDialog
    private var imageUri: Uri? = null

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
            uploadImageToFirebase(imageUri)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfilBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        dialogueProgress = ProgressDialog(requireContext())
        dialogueProgress.setTitle("Modification .....")
        dialogueProgress.setCanceledOnTouchOutside(false)
        loadInfos()

        binding.floatingAddPicActionButton.setOnClickListener {
            imagePickDialog()
        }
        binding.LogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            SharedPreferencesUtil.setUserLoggedIn(requireContext(), false)
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
        binding.buttonMyOffers.setOnClickListener {
            navigateToUserOffers()
        }


        return binding.root
    }

   /* private fun loadInfos() {
        val userId = auth.currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            val profileImageUrl = dataSnapshot.child("profileImage").getValue(String::class.java)
            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(this).load(profileImageUrl).into(binding.profilePic)
            }
            // Load other user information if needed
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to load profile info: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }*/
   private fun loadInfos() {
       val user = auth.currentUser
       if (user != null) {
           // Get user's display name from Firebase Auth
           val userName = user.displayName
           val userMail = user.email
           // Get user's profile picture URL from Firebase Database
           val userId = user.uid
           val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
           databaseReference.get().addOnSuccessListener { dataSnapshot ->
               val profileImageUrl = dataSnapshot.child("profileImage").getValue(String::class.java)
               if (!profileImageUrl.isNullOrEmpty()) {
                   // Check if the fragment is added to an activity and the activity is not null
                   if (isAdded && activity != null) {
                       Glide.with(this).load(profileImageUrl).into(binding.profilePic)
                   }
               }
           }.addOnFailureListener { e ->
               Toast.makeText(requireContext(), "Failed to load profile info: ${e.message}", Toast.LENGTH_SHORT).show()
           }

           // Set the display name to the TextView
           if (!userName.isNullOrEmpty()) {
               binding.profileSmya.text = userName
           }
           if(!userMail.isNullOrEmpty()){
               binding.tvEmail.text = userMail
           }
       } else {
           Toast.makeText(requireContext(), "User not signed in", Toast.LENGTH_SHORT).show()
       }
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
        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        cameraActivityResultLauncher.launch(imageUri)
    }

    private fun pickImageGallery() {
        galleryActivityResultLauncher.launch("image/*")
    }


    private fun uploadImageToFirebase(uri: Uri?) {
        if (uri == null) return

        val userId = auth.currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().getReference("profile_images/$userId.jpg")

        dialogueProgress.show()

        storageReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Update user's profile image URL in Firebase Database
                    FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .child("profileImage")
                        .setValue(downloadUri.toString())
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                            dialogueProgress.dismiss()
                            // Load the new image into the ImageView using Glide
                            Glide.with(this).load(downloadUri).into(binding.profilePic)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to upload image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            dialogueProgress.dismiss()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                dialogueProgress.dismiss()
            }
    }


    private fun navigateToUserOffers() {
        val intent = Intent(activity, UserOffersActivity::class.java)
        startActivity(intent)
    }
}
