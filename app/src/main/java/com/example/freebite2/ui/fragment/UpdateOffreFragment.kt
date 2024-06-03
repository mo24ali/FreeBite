package com.example.freebite2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.freebite2.R
import com.example.freebite2.databinding.FragmentUpdateOffreBinding
import com.example.freebite2.model.OffreModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class UpdateOffreFragment : Fragment() {

    private lateinit var offre: OffreModel // Assurez-vous d'avoir OffreModel
    private lateinit var binding: FragmentUpdateOffreBinding
    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var durationEditText: TextInputEditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_offre, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser votre vue avec les données de l'offre
        titleEditText.setText(offre.nameoffre)
        descriptionEditText.setText(offre.details)
        durationEditText.setText(offre.duration)

        // Mettre à jour l'offre
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
            findNavController().popBackStack()
        }
    }

    private fun updateOffre(offre: OffreModel) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("offres").child(offre.offerID.toString()).setValue(offre)
            .addOnSuccessListener {
                // Succès de la mise à jour
            }
            .addOnFailureListener {
                // Échec de la mise à jour
            }
    }
}
