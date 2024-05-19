package com.example.freebite2.ui.fragment
/*
import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.freebite2.R
import com.example.freebite2.databinding.FragmentProfilBinding
import com.example.freebite2.ui.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProfilFragment : Fragment() {
    private lateinit var binding: FragmentProfilBinding
    private lateinit var auth: FirebaseAuth
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private var imageUri: Uri? = null
    private val PERMISSION_REQUEST_CODE = 100
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfilBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.profileSmya.text = currentUser.displayName
            binding.tvEmail.text = currentUser.email
            // Load profile picture if available
            currentUser.photoUrl?.let { uri ->
                binding.profilePic.setImageURI(uri)
            }
        }

        binding.floatingAddPicActionButton.setOnClickListener {
            showImagePickDialog()
        }

        binding.tvEmail.setOnClickListener {
            findNavController().navigate(R.id.action_ProfilFragment_to_ChangeEmailFragment)
        }

        binding.LogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun showImagePickDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Image From")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> takePictureIntent()
                1 -> pickImageFromGallery()
            }
        }
        builder.show()
    }

    private fun takePictureIntent() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
        } else {
            startCameraIntent()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraIntent()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoUri: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    File.createTempFile("JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_", ".jpg", requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES))
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                imageUri = photoUri
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }


    private fun pickImageFromGallery() {
        val intGall = Intent(Intent.ACTION_PICK)
        intGall.type = "image/*"
        startActivityForResult(intGall, REQUEST_IMAGE_PICK)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            binding.profilePic.setImageURI(imageUri)
            uploadImageToFirebaseStorage()
        }
    }



    private fun uploadImageToFirebaseStorage() {
        if (imageUri == null) return

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Uploading Image...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReference("profileImages/${auth.currentUser?.uid}")
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    updateUserProfile(uri.toString())
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCanceledListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Image upload was cancelled", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bitmapToUri(bitmap: Bitmap): Uri {
        val cache = context?.cacheDir
        val file = File(cache, "${UUID.randomUUID()}.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
    }


    private fun updateUserProfile(profileImageUrl: String) {
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(profileImageUrl))
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.profilePic.setImageURI(Uri.parse(profileImageUrl))
                    Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
*/

 */
import android.Manifest
import android.app.AlertDialog
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
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.freebite2.databinding.FragmentProfilBinding
import com.example.freebite2.ui.activity.MainActivity
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
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return binding.root
    }

    private fun loadInfos() {
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
}
