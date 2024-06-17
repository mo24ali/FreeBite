package com.example.freebite2.ui.activity.admin


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import com.bumptech.glide.Glide
import com.example.freebite2.R
import com.example.freebite2.databinding.FragmentOffreDetailsBinding
import com.example.freebite2.model.OffreModel
import com.example.freebite2.ui.activity.UserOffersActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate

class AccessPostActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var offre: OffreModel
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var binding: FragmentOffreDetailsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentOffreDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("offres")

        // Get the offer object from intent extras
        offre = intent.getParcelableExtra<OffreModel>("offre")!!

        // Set up UI components
        setUpUI()
    }

    private fun setUpUI() {
        val offerPicDetails: ImageView = binding.offerPicDetails
        val offreName: TextView = binding.offreName
        val descriptionTextView: TextView = binding.descritptionRepas
        val providerTextView: TextView = binding.providerTextView

        val providerUID = offre.providerID
        val usersReference = FirebaseDatabase.getInstance().getReference("Users").child(providerUID.toString())
        usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val providerName = dataSnapshot.child("nom").value.toString()
                    providerTextView.text = providerName
                } else {
                    providerTextView.text = "Unknown Provider"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error fetching provider name: ${databaseError.message}")
            }
        })

        mapFragment = supportFragmentManager.findFragmentById(R.id.offerPosition) as SupportMapFragment



        Glide.with(this)
            .load(offre.pictureUrl)
            .into(offerPicDetails)

        offreName.text = offre.nameoffre
        descriptionTextView.text = offre.details

        mapFragment.getMapAsync(this)

        binding.backBtn2.setOnClickListener {
            onBackPressed()
        }




        manageButton()
    }




    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val offerLocation = LatLng(offre.latitude!!, offre.longitude!!)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.map_marker_logo).scale(90, 90)
        val markerOptions = MarkerOptions()
            .position(offerLocation)
            .title(offre.nameoffre)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        googleMap.addMarker(markerOptions)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(offerLocation, 15f))
    }

    private fun manageButton( ) {
           binding.takeOfferBtn.visibility = View.GONE

            binding.manageMyOffreBtn.visibility = View.GONE

    }
}
