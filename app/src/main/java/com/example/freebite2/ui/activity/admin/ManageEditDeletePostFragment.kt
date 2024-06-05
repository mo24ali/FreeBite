@file:Suppress("DEPRECATION")

package com.example.freebite2.ui.activity.admin

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ManageEditDeletePostFragment : Fragment() {

    private lateinit var offre: OffreModel
    private var _binding: FragmentUpdateOffreBinding? = null
    private val adminUid = "tZtTbchwFPOse0jhTXt39kCnNGo1"
    private val binding get() = _binding!!

    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var durationEditText: TextInputEditText
    private lateinit var offerPicDetails: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateOffreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        offerPicDetails = binding.uploadedImageView
        titleEditText = binding.titleEditText
        descriptionEditText = binding.descriptionEditText
        durationEditText = binding.durationEditText

        arguments?.let {
            offre = it.getParcelable("offre") ?: return@let
            initializeViews()
        }

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
                Log.e("ManageEditDeletePostFragment", "Offre mise à jour avec succès")
                Toast.makeText(requireContext(), "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e("ManageEditDeletePostFragment", "Échec de la mise à jour de l'offre")
                Toast.makeText(requireContext(), "Échec de la mise à jour de l'offre", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offre = (arguments?.getSerializable("offre") as? OffreModel)
            ?: throw IllegalArgumentException("Argument 'offre' must not be null")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.uid != adminUid) {
            Toast.makeText(requireContext(), "Vous n'êtes pas autorisé à modifier ce post", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
