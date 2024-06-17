package com.example.freebite2.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
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

    companion object {
        private const val REQUEST_CODE = 123
    }
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
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)


            Glide.with(this).load(bitmap).into(binding.uploadedImageView)
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