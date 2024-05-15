package com.example.freebite2.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.location.Location
import com.example.freebite2.R
import com.example.freebite2.databinding.ActivityAfterSignUpHelloBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AfterSignUpHelloActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAfterSignUpHelloBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterSignUpHelloBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val user = FirebaseAuth.getInstance().currentUser
        val displayName = user?.displayName

        if (!displayName.isNullOrEmpty()) {
            //val greetingText = getString(R.string.hello_user, displayName)
            val greetingText = getString(R.string.hello_user)
            val finalGreetingText = "$greetingText $displayName"
            binding.greetingTextView.text = finalGreetingText
        }
        // Request location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

// Get location
       /* fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Save location to Firebase
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val database = FirebaseDatabase.getInstance().getReference("Users")
                        database.child(user.uid).child("location").setValue(location)

                        // Start MapsActivity
                        val intent = Intent(this, MapActivity::class.java)
                        intent.putExtra("location", location)
                        startActivity(intent)
                    }
                }
            }*/
        // Create an instance of YourViewModel with fullName parameter
        // val fullName : String = finalGreetingText // Replace this with the actual full name
     //   binding.viewModel = NameViewModel(fullName) // Set the ViewModel
        // Execute pending bindings
     //   binding.executePendingBindings()

        binding.verificationBtn.setOnClickListener{
            startActivity(Intent(this,MapActivity::class.java))
        }
    }
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
