package com.example.freebite2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.freebite2.ui.activity.AfterSignUpHelloActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class LocationActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        updateLocation()
    }

    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val user = FirebaseAuth.getInstance().currentUser
                    saveLocationToFirebase(user!!, location)
                }
            }
    }

    private fun saveLocationToFirebase(user: FirebaseUser, location: Location) {
        val locationData = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )
        val database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(user.uid).setValue(locationData)
            .addOnSuccessListener {
                Toast.makeText(this@LocationActivity, "Location saved successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AfterSignUpHelloActivity::class.java))
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@LocationActivity, "Failed to save location", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }
}