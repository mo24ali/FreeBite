package com.example.freebite2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.freebite2.databinding.FragmentUpdateOffreBinding
import com.example.freebite2.model.OffreModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class UpdateOffreFragment : Fragment() {

    private lateinit var offre: OffreModel
    private var _binding: FragmentUpdateOffreBinding? = null
    private val binding get() = _binding!!

    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var durationEditText: TextInputEditText


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateOffreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize your views here
        val offerPicDetails: ImageView = binding.uploadedImageView
        titleEditText = binding.titleEditText
        descriptionEditText = binding.descriptionEditText
        durationEditText = binding.durationEditText


        arguments?.let {
            offre = it.getParcelable("offre")!!
            initializeViews()
        }
        Glide.with(this)
            .load(offre.pictureUrl)
            .into(offerPicDetails)
        binding.modifyOfferPic.setOnClickListener {

        }

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
         // parentFragmentManager.popBackStack()
            onDestroy()
        }
    }

    private fun initializeViews() {
        titleEditText.setText(offre.nameoffre)
        descriptionEditText.setText(offre.details)
        durationEditText.setText(offre.duration)
    }

    private fun updateOffre(offre: OffreModel) {
        // Ensure the fragment is attached to a context before proceeding
        if (!isAdded) return

        val database = FirebaseDatabase.getInstance().reference
        database.child("offres").child(offre.offerID.toString()).setValue(offre)
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Échec de la mise à jour de l'offre", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





/*
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.freebite2.databinding.FragmentUpdateOffreBinding
import com.example.freebite2.model.OffreModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class UpdateOffreFragment : Fragment() {

    private lateinit var offre: OffreModel
    private var _binding: FragmentUpdateOffreBinding? = null
    private val binding get() = _binding!!

    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var durationEditText: TextInputEditText

    private var imageUri: Uri? = null
    private var imageUploadOffre: Uri? = null

    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            uploadImageToFirebase(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateOffreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize your views here
        titleEditText = binding.titleEditText
        descriptionEditText = binding.descriptionEditText
        durationEditText = binding.durationEditText

        // Assuming 'offre' is passed as an argument to the fragment
        arguments?.let {
            offre = it.getParcelable("offre")!!
            initializeViews()
        }

        binding.modifyOfferPic.setOnClickListener {
            imagePickDialog()
        }

        binding.btnsubmit.setOnClickListener {
            val updatedOffre = OffreModel(
                offre.providerID,
                offre.offerID,
                titleEditText.text.toString(),
                descriptionEditText.text.toString(),
                durationEditText.text.toString(),
                offre.latitude,
                offre.longitude,
                imageUploadOffre?.toString() ?: offre.pictureUrl
            )

            updateOffre(updatedOffre)
            onDestroy()
        }
    }

    private fun initializeViews() {
        titleEditText.setText(offre.nameoffre)
        descriptionEditText.setText(offre.details)
        durationEditText.setText(offre.duration)
    }

    private fun imagePickDialog() {
        val options = arrayOf("Galerie")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choisir une image depuis la galerie")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    pickImageGallery()
                }
            }
        }
        builder.setOnCancelListener {
            // Allow reopening the dialog if it was dismissed
            binding.modifyOfferPic.isEnabled = true
        }
        builder.show()
    }

    private fun pickImageGallery() {
        galleryActivityResultLauncher.launch("image/*")
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val storageReference = FirebaseDatabase.getInstance().reference.child("offer_images").child(UUID.randomUUID().toString())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = storageReference.putFile(uri).await()
                val downloadUri = snapshot.storage.downloadUrl.await()
                imageUploadOffre = downloadUri
                withContext(Dispatchers.Main) {
                    Glide.with(requireContext()).load(uri).into(binding.uploadedImageView)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateOffre(offre: OffreModel) {
        // Ensure the fragment is attached to a context before proceeding
        if (!isAdded) return

        val database = FirebaseDatabase.getInstance().reference
        database.child("offres").child(offre.offerID.toString()).setValue(offre)
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Échec de la mise à jour de l'offre", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

 */
*/