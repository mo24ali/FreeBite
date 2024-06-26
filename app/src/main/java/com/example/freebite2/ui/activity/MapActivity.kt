package com.example.freebite2.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import com.example.freebite2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
                Toast.makeText(this, "échec de capturer ta position", Toast.LENGTH_SHORT).show()
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
                            googleMap.clear() // clean the map
                            map = googleMap

                            // Zoom current location on map
                            val currentLocation = LatLng(currentLat, currentLong)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                            // Add a marker at the current location
                            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.map_marker_logo).scale(120,120)
                            val markerOptions = MarkerOptions().position(currentLocation)
                                                           .title("Current Location")
                                                           .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            map.addMarker(markerOptions)
                        }
                    } else {
                        Toast.makeText(this@MapActivity, "Echec d'obtenir ta position actuelle", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Localisation capturée en succés", Toast.LENGTH_SHORT).show()
                    // Start new Activity
                    val intent = Intent(this, MainHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "échec d'enregistrer ta position", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 44) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // When permission granted, get current location
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Permission refusé.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
