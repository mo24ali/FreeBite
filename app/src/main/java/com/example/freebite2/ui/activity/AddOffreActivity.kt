package com.example.freebite2.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.freebite2.R
import com.example.freebite2.databinding.ActivityAddOffreBinding
import com.example.freebite2.model.OffreModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddOffreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddOffreBinding
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_offre)
        database = FirebaseDatabase.getInstance().getReference("offres")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnAddOffre.setOnClickListener {
            val offreName = binding.etOffreName.text.toString().trim()
            val offreDescription = binding.etOffreDescription.text.toString().trim()
            val offreDuration = binding.etOffreDuration.text.toString().trim()

            if (offreName.isNotEmpty() && offreDescription.isNotEmpty() && offreDuration.isNotEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                } else {
                    getLastLocation(userId, offreDescription)
                }
            } else {
                showToast("Please fill in all fields")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val offreDescription = binding.etOffreDescription.text.toString().trim()
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                getLastLocation(userId, offreDescription)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLastLocation(userId: String, offreDescription: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission is not granted", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val offreId = database.push().key ?: ""
                val offre = OffreModel(userId, offreId, offreDescription, null, latitude, longitude)

                database.child(offreId).setValue(offre).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.etOffreName.text.clear()
                        binding.etOffreDescription.text.clear()
                        binding.etOffreDuration.text.clear()
                        showToast("Offer added successfully")
                    } else {
                        showToast("Failed to add offer")
                    }
                }
            } else {
                Toast.makeText(this, "Unable to get location. Make sure location is enabled on the device.", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error obtaining location: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
