package com.example.freebite2.ui.activity

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.freebite2.databinding.ActivityAfterSignUpHelloBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AfterSignUpHelloActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAfterSignUpHelloBinding
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private lateinit var settingsClient: SettingsClient
    private val REQUEST_CHECK_SETTINGS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterSignUpHelloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsClient = LocationServices.getSettingsClient(this)

        // Create location request
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        // Build location settings request
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        locationSettingsRequest = builder.build()

        binding.verificationBtn.setOnClickListener {
            checkGPSAndProceed()
        }
    }

    private fun checkGPSAndProceed() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener(this) {
                // GPS is enabled, start MapActivity
                startMapActivity()
            }
            .addOnFailureListener(this) { exception ->
                if (exception is ResolvableApiException) {
                    // GPS is not enabled, show toast message
                    Toast.makeText(this, "Please enable GPS to proceed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // GPS was enabled, start MapActivity
                startMapActivity()
            } else {
                Toast.makeText(this, "GPS is required to proceed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}