package com.example.freebite2.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.freebite2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
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

        /*val dashBoardBtn = findViewById<Button>(R.id.nextActivityButtonDashBoard)
        dashBoardBtn.setOnClickListener {
            val intent = Intent(this, MainHomeActivity::class.java)
            startActivity(intent)
        }*/

        btFind.setOnClickListener {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=$currentLat,$currentLong" +
                    "&radius=500" +
                    "&sensor=true" +
                    "&key=" + getString(R.string.google_maps_api_key)

            // Execute place task method to download JSON data
            lifecycleScope.launch(Dispatchers.IO) {
                val data = downloadUrl(url)
                // Parse data here and update UI on main thread
                launch(Dispatchers.Main) {
                    // Update the UI with the parsed data
                }
            }
            Toast.makeText(this@MapActivity, "Tout est bon ! on passe à la page principale", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainHomeActivity::class.java)
            startActivity(intent)

        }

        // Get location and save to Firebase
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Save location to Firebase
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val database = FirebaseDatabase.getInstance().getReference("Users")
                        database.child(user.uid).child("location").setValue(location)
                    }
                }
            }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLong = location.longitude
                currentLat = location.latitude
                supportMapFragment.getMapAsync { googleMap ->
                    map = googleMap
                    // Zoom current location on map
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLat, currentLong), 10f))
                    // Add a marker at the current location
                    map.addMarker(MarkerOptions().position(LatLng(currentLat, currentLong)).title("Current Location"))
                }
            }
        }
    }

    private suspend fun downloadUrl(str: String): String {
        val url = URL(str)
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 44) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // When permission granted, get current location
                getCurrentLocation()
            }
        }
    }
}