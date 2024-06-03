package com.example.freebite2.ui.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.databinding.ActivityEditOfferBinding
import com.example.freebite2.model.OffreModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditOfferActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditOfferBinding
    private lateinit var dialogueProgress: ProgressDialog
    private lateinit var auth: FirebaseAuth

    private lateinit var offerModel: OffreModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialogueProgress = ProgressDialog(this)
        dialogueProgress.setMessage("Mise à jour de l'offre...")
        dialogueProgress.setCancelable(false)

        auth = FirebaseAuth.getInstance()

        // Récupérer les données de l'intent
        offerModel = intent.getSerializableExtra("offerModel") as OffreModel

        // Afficher les données actuelles dans les champs d'édition
        binding.titleEditText.setText(offerModel.nameoffre)
        binding.descriptionEditText.setText(offerModel.details)
        binding.durationEditText.setText(offerModel.duration)

        binding.btnsubmit.setOnClickListener {
            updateOffer()
        }
    }

    private fun updateOffer() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Utilisateur non authentifié", Toast.LENGTH_SHORT).show()
            return
        }

        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val duration = binding.durationEditText.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || duration.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        // Mettre à jour l'objet OffreModel existant avec les nouvelles données
        offerModel.nameoffre = title
        offerModel.details = description
        offerModel.duration = duration

        val offreRef = FirebaseDatabase.getInstance().getReference("offres").child(offerModel.offerID ?: "")
        dialogueProgress.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                offreRef.setValue(offerModel).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditOfferActivity, "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show()
                    dialogueProgress.dismiss()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditOfferActivity, "Échec de la mise à jour de l'offre: ${e.message}", Toast.LENGTH_SHORT).show()
                    dialogueProgress.dismiss()
                }
            }
        }
    }

}
