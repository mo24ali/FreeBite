package com.example.freebite2.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.freebite2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MapActivity : AppCompatActivity() {
    private lateinit var btFind: Button
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLat = 0.0
    private var currentLong = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Initialize variables
        btFind = findViewById(R.id.dashboardButton)
        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // If permission granted, get current location
            getCurrentLocation()
        } else {
            // When permission denied, request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 44)
        }

        btFind.setOnClickListener {
            // Check if location is captured
            if (currentLat != 0.0 && currentLong != 0.0) {
                saveLocationToFirebase()
            } else {
                Toast.makeText(this, "Location is not captured yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update location every 10 seconds
            fastestInterval = 5000 // Update location every 5 seconds in the fastest case
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations){
                    if (location != null) {
                        currentLong = location.longitude
                        currentLat = location.latitude
                        supportMapFragment.getMapAsync { googleMap ->
                            map = googleMap
                            // Zoom current location on map
                            val currentLocation = LatLng(currentLat, currentLong)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                            // Add a marker at the current location
                            map.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                        }
                    } else {
                        Toast.makeText(this@MapActivity, "Failed to get current location", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }, Looper.getMainLooper())
    }

    private fun saveLocationToFirebase() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val database = FirebaseDatabase.getInstance().getReference("Users")
            val locationData = mapOf("latitude" to currentLat, "longitude" to currentLong)
            database.child(user.uid).child("location").setValue(locationData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Location saved successfully", Toast.LENGTH_SHORT).show()
                    // Start new Activity
                    val intent = Intent(this, MainHomeActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save location", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 44) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // When permission granted, get current location
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Permission denied. Unable to get location.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
