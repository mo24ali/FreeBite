@file:Suppress("DEPRECATION")

package com.example.freebite2.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.freebite2.R
import com.example.freebite2.model.OffreModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EditOfferActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var motionLayout: MotionLayout
    private lateinit var titleTextView: TextView
    private lateinit var backButton: ImageButton
    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var durationEditText: TextInputEditText
    private lateinit var mapView: SupportMapFragment
    private lateinit var submitButton: MaterialButton

    private var offerModel: OffreModel? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_offer)

        motionLayout = findViewById(R.id.main)
        titleTextView = findViewById(R.id.textviewadpost)
        backButton = findViewById(R.id.btnbackhome)
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        durationEditText = findViewById(R.id.DureeModifEditText)
        mapView = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        submitButton = findViewById(R.id.btnsubmit)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Récupérer l'offre à modifier depuis l'intent ou autre source
        offerModel = intent.getParcelableExtra("offre")

        // Remplir les champs avec les données de l'offre
        offerModel?.let {
            titleEditText.setText(it.nameoffre)
            descriptionEditText.setText(it.details)
            durationEditText.setText(it.duration)
            updateMapLocation(it.latitude ?: 0.0, it.longitude ?: 0.0)
        }

        mapView.getMapAsync(this)

        backButton.setOnClickListener {
            onBackPressed()
        }

        submitButton.setOnClickListener {
            // Enregistrer les modifications de l'offre
            saveOfferChanges()
        }

        // Demander la permission d'accès à la localisation si ce n'est pas déjà fait
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Obtenir la dernière position connue
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        updateMapLocation(it.latitude, it.longitude)
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun updateMapLocation(latitude: Double, longitude: Double) {
        offerModel?.apply {
            this.latitude = latitude
            this.longitude = longitude

            // Mettre à jour la carte avec la nouvelle position
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val location = LatLng(latitude, longitude)
                googleMap.addMarker(MarkerOptions().position(location).title("Marker"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        }
    }

    private fun saveOfferChanges() {
        offerModel?.apply {
            nameoffre = titleEditText.text.toString()
            details = descriptionEditText.text.toString()
            duration = durationEditText.text.toString()
            // TODO: Mettre à jour latitude et longitude si modifiées
        }

        // Sauvegarder l'offre modifiée, par exemple dans Firebase
        // Exemple: FirebaseDatabase.getInstance().getReference("offres").child(offerModel.offerID).setValue(offerModel)
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLastLocation()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }
}


