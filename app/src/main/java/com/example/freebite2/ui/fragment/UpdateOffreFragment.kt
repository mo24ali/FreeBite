package com.example.freebite2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
        titleEditText = binding.titleEditText
        descriptionEditText = binding.descriptionEditText
        durationEditText = binding.durationEditText

        // Assuming 'offre' is passed as an argument to the fragment
        arguments?.let {
            offre = it.getParcelable("offre")!!
            initializeViews()
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


