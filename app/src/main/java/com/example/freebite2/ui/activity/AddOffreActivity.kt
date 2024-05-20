package com.example.freebite2.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.freebite2.databinding.ActivityAddOffreBinding
import com.example.freebite2.model.OffreModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class AddOffreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddOffreBinding
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("offres")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOffreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val offerRef = myRef.child("your_offer_id")
        offerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val offre = dataSnapshot.getValue(OffreModel::class.java)
                // Utilisez l'objet 'offre'
            }

            override fun onCancelled(error: DatabaseError) {
                // Échec de la lecture de la valeur
                Log.w("Firebase", "Failed to read value.", error.toException())
            }
        })

        // Vérifier et demander les permissions nécessaires
        checkPermissions()

        binding.uploadImgBtn.setOnClickListener {
            uploadImage()
            binding.uploadImgBtn.visibility = android.view.View.GONE
            binding.offerPicLayout.visibility = android.view.View.VISIBLE
        }
    }

    private fun checkPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: com.karumi.dexter.MultiplePermissionsReport?) {
                    if (report != null) {
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(this@AddOffreActivity, "Toutes les permissions ont été accordées", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AddOffreActivity, "Permissions refusées", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun uploadImage() {
        Dexter.withContext(this)
            .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(intent, 101)
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(this@AddOffreActivity, "Permission refusée", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
                    p1?.continuePermissionRequest()
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            binding.offerPic.setImageURI(data?.data)
        }
    }
}
